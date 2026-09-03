package io.github.saifullah.nurani.ads.ironsource

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import com.unity3d.mediation.LevelPlay
import com.unity3d.mediation.LevelPlayAdError
import com.unity3d.mediation.LevelPlayAdInfo
import com.unity3d.mediation.LevelPlayAdSize
import com.unity3d.mediation.banner.LevelPlayBannerAdView
import com.unity3d.mediation.banner.LevelPlayBannerAdViewListener
import io.github.saifullah.nurani.ads.core.utils.ContextUtils.Companion.findActivity
import io.github.saifullah.nurani.ads.core.AdFailedRetryRule
import io.github.saifullah.nurani.ads.core.AdFailedRetryRule.Companion.exponentialDefault
import io.github.saifullah.nurani.ads.core.AdLogger
import io.github.saifullah.nurani.ads.core.AdRefreshStrategy.Companion.disable
import io.github.saifullah.nurani.ads.core.AdReloadPolicy
import io.github.saifullah.nurani.ads.core.AdSize
import io.github.saifullah.nurani.ads.core.AdStateManager
import io.github.saifullah.nurani.ads.core.BannerAd
import io.github.saifullah.nurani.ads.core.BannerAdListener
import io.github.saifullah.nurani.ads.core.Scheduler
import io.github.saifullah.nurani.ads.core.exponentialRetry
import io.github.saifullah.nurani.ads.core.linearRetry
import io.github.saifullah.nurani.ads.core.utils.DefaultAdLogger

class IronSourceBannerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    private var bannerLayout: LevelPlayBannerAdView? = null
    private var bannerAd: BannerAd<AdSize>? = null
    private var currentSize: AdSize = AdSize.BANNER

    val adSize: AdSize get() = currentSize
    private var adUnitId: String? = null
    private var placementId: String? = null
    private var placementName: String? = null
    private var logger: AdLogger? = DefaultAdLogger(TAG)
    private var reloadPolicies: Set<AdReloadPolicy> = emptySet()
    var retryRule: AdFailedRetryRule = exponentialDefault()

    private var keepAdSlot = true
    private var stateManager: AdStateManager? = null
    var adListener: BannerAdListener? = null
    private var testMode = false
    private var requestTag: String? = null

    init {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        if (attrs != null) {
            val a = context.obtainStyledAttributes(attrs, R.styleable.IronSourceBannerView)

            try {
                val reloadFlags = a.getInt(R.styleable.IronSourceBannerView_adReloadPolicies, 0)
                reloadPolicies = parseReloadPolicies(reloadFlags)
                keepAdSlot = a.getBoolean(R.styleable.IronSourceBannerView_keepAdSlot, true)
                val retryType = a.getInt(R.styleable.IronSourceBannerView_adFailedRetryRule, 3)

                val delay = a.getInt(
                    R.styleable.IronSourceBannerView_adFailedLoadDelayMillis,
                    AdFailedRetryRule.DEFAULT_DELAY_MS.toInt()
                ).toLong()

                val attempts = a.getInt(
                    R.styleable.IronSourceBannerView_adFailedLoadMaxAttempt,
                    AdFailedRetryRule.DEFAULT_MAX_ATTEMPTS
                )

                val multiplier = a.getFloat(
                    R.styleable.IronSourceBannerView_adFailedLoadMultiplier,
                    AdFailedRetryRule.DEFAULT_MULTIPLIER
                )

                retryRule = buildRetryRule(retryType, delay, attempts, multiplier)

                val sizeFlags = a.getInt(R.styleable.IronSourceBannerView_adFormats, 1)
                val loadType = a.getInt(R.styleable.IronSourceBannerView_adLoadType, 1)

                adUnitId = a.getString(R.styleable.IronSourceBannerView_adUnitId)
                placementId = a.getString(R.styleable.IronSourceBannerView_placementId)
                val sizes = parseSizes(sizeFlags)

                bannerAd = if (loadType == 2) {
                    BannerAd.Random(*sizes.toTypedArray())
                } else {
                    BannerAd.Fixed(sizes[0])
                }
            } finally {
                a.recycle()
            }
        }
        if (!adUnitId.isNullOrEmpty() || !placementId.isNullOrEmpty()) loadAd()
    }

    private fun parseReloadPolicies(flags: Int): Set<AdReloadPolicy> {
        val list: MutableSet<AdReloadPolicy> = HashSet()

        if ((flags and 1) != 0) list.add(AdReloadPolicy.OnClicked)
        if ((flags and 2) != 0) list.add(AdReloadPolicy.OnDismissed)
        if ((flags and 4) != 0) list.add(AdReloadPolicy.OnFailedToShow)

        return list
    }

    private fun buildRetryRule(
        type: Int,
        delay: Long,
        attempts: Int,
        multiplier: Float
    ): AdFailedRetryRule {
        return when (type) {
            1 -> AdFailedRetryRule.none()
            2 -> linearRetry {
                maxAttempts = attempts
                delayInMillis = delay
            }

            3 -> exponentialRetry {
                maxAttempts = attempts
                delayInMillis = delay
                this.multiplier = multiplier
            }

            else -> exponentialRetry()
        }
    }

    private fun parseSizes(flags: Int): List<AdSize> {
        val sizes: MutableList<AdSize> = mutableListOf()

        if ((flags and 1) != 0) sizes.add(AdSize.BANNER)
        if ((flags and 2) != 0) sizes.add(AdSize.MEDIUM_RECTANGLE)
        if ((flags and 4) != 0) sizes.add(AdSize.LARGE_BANNER)

        if (sizes.isEmpty()) {
            sizes.add(AdSize.BANNER)
        }

        return sizes
    }

    fun loadAd() {
        if (!IronSourceAds.isInitialized()) {
            IronSourceAds.runWhenInitialized {
                loadAd()
            }
            return
        }
        if (stateManager == null) {
            stateManager =
                AdStateManager(reloadPolicies, retryRule, disable(), null, Scheduler(null), requestTag ?: TAG) {
                    loadAdInternally(context)
                }
        }

        stateManager!!.loadAd()
    }

    private fun mapToLevelPlayAdSize(context: Context, adSize: AdSize): LevelPlayAdSize {
        return when {
            adSize == AdSize.BANNER -> LevelPlayAdSize.BANNER
            adSize.height >= 250 -> LevelPlayAdSize.MEDIUM_RECTANGLE
            adSize.height >= 90 -> LevelPlayAdSize.LARGE
            adSize == AdSize.SMART_BANNER || adSize == AdSize.FLUID -> LevelPlayAdSize.createAdaptiveAdSize(context) ?: LevelPlayAdSize.BANNER
            else -> LevelPlayAdSize.createCustomSize(adSize.width, adSize.height)
        }
    }

    @SuppressLint("MissingPermission")
    private fun loadAdInternally(context: Context) {
        if (testMode) {
            LevelPlay.setAdaptersDebug(true)
        }
        val finalAdUnitId = (adUnitId ?: placementId)?.takeIf { it.isNotBlank() }
            ?: if (testMode) TEST_AD_UNIT_ID else null
        if (!testMode && finalAdUnitId.isNullOrEmpty()) {
            checkNotNull(finalAdUnitId) { "adUnitId or placementId must be set." }
            require(finalAdUnitId.isNotEmpty()) { "adUnitId must not be empty." }
        }
        if (!keepAdSlot) {
            visibility = GONE
        }
        destroyAd(resetStateManager = false)
        currentSize = bannerAd?.getSize() ?: AdSize.BANNER

        val levelPlaySize = mapToLevelPlayAdSize(context, currentSize)
        val configBuilder = LevelPlayBannerAdView.Config.Builder()
            .setAdSize(levelPlaySize)
        placementName?.let { configBuilder.setPlacementName(it) }

        val activity = findActivity(context) ?: context
        val banner = LevelPlayBannerAdView(activity, finalAdUnitId!!, configBuilder.build())
        bannerLayout = banner

        banner.setBannerListener(object : LevelPlayBannerAdViewListener {
            override fun onAdLoaded(adInfo: LevelPlayAdInfo) {
                log("Banner loaded")
                stateManager?.onAdLoaded()
                adListener?.onAdLoaded()
                fadeIn()
            }

            override fun onAdLoadFailed(error: LevelPlayAdError) {
                log("Load failed: ${error.errorMessage}")
                val adError = IronSourceUtils.adErrorFrom(error)
                stateManager?.onAdFailedToLoad(adError)
                if (stateManager?.isRetryingAdFailedLoad != true) {
                    adListener?.onAdFailedToLoad(adError)
                }
                if (!keepAdSlot) visibility = GONE
            }

            override fun onAdDisplayed(adInfo: LevelPlayAdInfo) {
                log("Ad Displayed")
                adListener?.onAdDisplayed()
                stateManager?.onAdDisplayed()
            }

            override fun onAdDisplayFailed(adInfo: LevelPlayAdInfo, error: LevelPlayAdError) {
                log("Ad Display Failed: ${error.errorMessage}")
                val adError = IronSourceUtils.adErrorFrom(error)
                stateManager?.onAdFailedToShow(adError)
                adListener?.onAdFailedToShow(adError)
            }

            override fun onAdClicked(adInfo: LevelPlayAdInfo) {
                log("Ad clicked")
                adListener?.onAdClicked()
                stateManager?.onAdClicked()
            }

            override fun onAdExpanded(adInfo: LevelPlayAdInfo) {
                log("Ad Expanded")
                adListener?.onAdShowed()
                stateManager?.onAdShowed()
            }

            override fun onAdCollapsed(adInfo: LevelPlayAdInfo) {
                log("Ad Collapsed")
                stateManager?.onAdDismissed()
                adListener?.onAdDismissed()
            }

            override fun onAdLeftApplication(adInfo: LevelPlayAdInfo) {
                log("Ad Left Application")
            }
        })

        val width = LayoutParams.MATCH_PARENT
        val heightPx = if (levelPlaySize.height > 0) {
            (levelPlaySize.height * context.resources.displayMetrics.density).toInt()
        } else {
            (50 * context.resources.displayMetrics.density).toInt()
        }
        banner.layoutParams = LayoutParams(width, heightPx)

        addView(banner)
        log("Loading banner adUnitId=$finalAdUnitId requestedSize=${currentSize.width}x${currentSize.height} levelPlaySize=${levelPlaySize.width}x${levelPlaySize.height} layout=${width}x$heightPx")
        banner.loadAd()
    }

    private fun fadeIn() {
        if (!keepAdSlot) setAlpha(0f)
        visibility = VISIBLE
        if (!keepAdSlot) {
            animate()
                .alpha(1f)
                .setDuration(250)
                .start()
        }
    }

    private fun destroyAd(resetStateManager: Boolean = true) {
        val banner = bannerLayout
        if (banner != null) {
            removeView(banner)
            banner.destroy()
            bannerLayout = null
        }
        if (resetStateManager) {
            stateManager?.onDestroy()
            stateManager = null
        }
    }

    private fun log(msg: String?) {
        logger?.d("$TAG : $msg")
    }

    fun setAdLogger(logger: AdLogger?) {
        this.logger = logger ?: DefaultAdLogger(TAG)
    }

    fun setPlacementId(id: String) {
        this.placementId = id
        if (this.adUnitId == null) {
            this.adUnitId = id
        }
    }

    fun setAdUnitId(id: String) {
        this.adUnitId = id
    }

    fun setPlacementName(name: String) {
        this.placementName = name
    }

    fun setAdSize(size: AdSize) {
        bannerAd = BannerAd.Fixed(size)
    }

    fun setAdSize(size: LevelPlayAdSize) {
        bannerAd = BannerAd.Fixed(AdSize(size.width, size.height))
    }

    fun setBannerAd(size: BannerAd<AdSize>) {
        bannerAd = size
    }

    fun setTestModeEnabled(enabled: Boolean) {
        this.testMode = enabled
    }

    fun setRequestTag(tag: String?) {
        this.requestTag = tag
    }

    fun setKeepAdSlot(keepAdSlot: Boolean) {
        this.keepAdSlot = keepAdSlot
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        resume()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        pause()
    }

    fun pause() {
        bannerLayout?.pauseAutoRefresh()
        stateManager?.onStop()
    }

    fun resume() {
        bannerLayout?.resumeAutoRefresh()
        stateManager?.onStart()
    }

    fun destroy() {
        destroyAd(resetStateManager = true)
    }

    companion object {
        const val TEST_AD_UNIT_ID: String = "ll7laet2x8ilqdee"
        const val TAG: String = "IronSourceBannerView"
    }
}
