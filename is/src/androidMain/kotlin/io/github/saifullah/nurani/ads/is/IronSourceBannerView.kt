package io.github.saifullah.nurani.ads.`is`

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import com.ironsource.mediationsdk.ISBannerSize
import com.ironsource.mediationsdk.IronSource
import com.ironsource.mediationsdk.IronSourceBannerLayout
import com.ironsource.mediationsdk.adunit.adapter.utility.AdInfo
import com.ironsource.mediationsdk.logger.IronSourceError
import com.ironsource.mediationsdk.sdk.LevelPlayBannerListener
import io.github.saifullah.nurani.ads.core.AdFailedRetryRule
import io.github.saifullah.nurani.ads.core.AdFailedRetryRule.Companion.exponentialDefault
import io.github.saifullah.nurani.ads.core.AdLogger
import io.github.saifullah.nurani.ads.core.AdRefreshStrategy.Companion.disable
import io.github.saifullah.nurani.ads.core.AdReloadPolicy
import io.github.saifullah.nurani.ads.core.AdStateManager
import io.github.saifullah.nurani.ads.core.BannerAd
import io.github.saifullah.nurani.ads.core.BannerAdListener
import io.github.saifullah.nurani.ads.core.Scheduler
import io.github.saifullah.nurani.ads.core.exponentialRetry
import io.github.saifullah.nurani.ads.core.linearRetry
import io.github.saifullah.nurani.ads.core.utils.ContextUtils.Companion.findActivity

class IronSourceBannerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    private var bannerLayout: IronSourceBannerLayout? = null
    private var bannerAd: BannerAd<ISBannerSize>? = null
    private var currentSize: ISBannerSize = ISBannerSize.BANNER

    val adSize: ISBannerSize get() = currentSize
    private var placementId: String? = null
    private var logger: AdLogger? = null
    private var reloadPolicies: Set<AdReloadPolicy> = emptySet()
    var retryRule: AdFailedRetryRule = exponentialDefault()

    private var keepAdSlot = true
    private var stateManager: AdStateManager? = null
    var adListener: BannerAdListener? = null
    private var testMode = false

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
        if (!placementId.isNullOrEmpty()) loadAd()
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

    private fun parseSizes(flags: Int): List<ISBannerSize> {
        val sizes: MutableList<ISBannerSize> = mutableListOf()

        if ((flags and 1) != 0) sizes.add(ISBannerSize.BANNER)
        if ((flags and 2) != 0) sizes.add(ISBannerSize.RECTANGLE)
        if ((flags and 4) != 0) sizes.add(ISBannerSize.LARGE)

        if (sizes.isEmpty()) {
            sizes.add(ISBannerSize.BANNER)
        }

        return sizes
    }

    fun loadAd() {
        if (stateManager == null) {
            stateManager =
                AdStateManager(reloadPolicies, retryRule, disable(), null, Scheduler(null), TAG) {
                    loadAdInternally(context)
                }
        }

        stateManager!!.loadAd()
    }

    @SuppressLint("MissingPermission")
    private fun loadAdInternally(context: Context) {
        if (!testMode) {
            checkNotNull(placementId) { "placementId must be set." }
            require(placementId!!.isNotEmpty()) { "placementId must not be empty." }
        }
        if (!keepAdSlot) {
            visibility = GONE
        }
        if (!keepAdSlot) {
            visibility = GONE
        }
        destroyAd()
        currentSize = bannerAd?.getSize() ?: ISBannerSize.BANNER

        val activity = findActivity(context)
        checkNotNull(activity) { "Context must be an Activity to load IronSource Banner." }

        val banner = IronSource.createBanner(activity, currentSize)
        bannerLayout = banner

        banner.levelPlayBannerListener = object : LevelPlayBannerListener {
            override fun onAdLoaded(adInfo: AdInfo) {
                log("Banner loaded")
                stateManager?.onAdLoaded()
                adListener?.onAdLoaded()
                fadeIn()
            }

            override fun onAdLoadFailed(error: IronSourceError) {
                log("Load failed: ${error.errorMessage}")
                val adError = IronSourceUtils.adErrorFrom(error)
                stateManager?.onAdFailedToLoad(adError)
                adListener?.onAdFailedToLoad(adError)
                if (!keepAdSlot) visibility = GONE
            }

            override fun onAdClicked(adInfo: AdInfo) {
                log("Ad clicked")
                adListener?.onAdClicked()
                stateManager?.onAdClicked()
            }

            override fun onAdScreenPresented(adInfo: AdInfo) {
                log("Ad Showed")
                adListener?.onAdDisplayed()
                stateManager?.onAdDisplayed()
            }

            override fun onAdScreenDismissed(adInfo: AdInfo) {
                log("Ad Dismissed")
                stateManager?.onAdDismissed()
                adListener?.onAdDismissed()
            }

            override fun onAdLeftApplication(adInfo: AdInfo) {
            }
        }

        val width = LayoutParams.MATCH_PARENT
        val heightPx = (currentSize.height * context.resources.displayMetrics.density).toInt()
        banner.layoutParams = LayoutParams(width, heightPx)

        addView(banner)
        val finalPlacementId = if (testMode) TEST_AD_UNIT_ID else placementId
        IronSource.loadBanner(banner, finalPlacementId)

        log("Loading banner...")
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

    private fun destroyAd() {
        val banner = bannerLayout
        if (banner != null) {
            removeView(banner)
            stateManager?.onDestroy()
            IronSource.destroyBanner(banner)
            bannerLayout = null
            stateManager = null
        }
    }

    private fun log(msg: String?) {
        logger?.d("$TAG : $msg")
    }

    fun setAdLogger(logger: AdLogger?) {
        this.logger = logger
    }

    fun setPlacementId(id: String) {
        this.placementId = id
    }

    fun setAdSize(size: ISBannerSize) {
        bannerAd = BannerAd.fixed(size)
    }

    fun setBannerAd(size: BannerAd<io.github.saifullah.nurani.ads.core.AdSize>) {
        bannerAd = size.mapToBannerAd {
            when {
                it.height >= 250 -> ISBannerSize.RECTANGLE
                it.height >= 90 -> ISBannerSize.LARGE
                else -> ISBannerSize.BANNER
            }
        }
    }

    fun setTestModeEnabled(enabled: Boolean) {
        this.testMode = enabled
    }

    fun setKeepAdSlot(keepAdSlot: Boolean) {
        this.keepAdSlot = keepAdSlot
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        destroyAd()
    }

    fun pause() {
    }

    fun resume() {
    }

    fun destroy() {
        destroyAd()
    }

    companion object {
        const val TEST_AD_UNIT_ID: String = "ch132493tceqkqsg"
        const val TAG: String = "IronSourceBannerView"
    }
}
