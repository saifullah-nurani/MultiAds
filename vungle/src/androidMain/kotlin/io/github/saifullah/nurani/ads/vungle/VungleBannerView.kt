package io.github.saifullah.nurani.ads.vungle

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import com.vungle.ads.BaseAd
import com.vungle.ads.BaseAdListener
import com.vungle.ads.VungleAdSize
import com.vungle.ads.VungleBannerView as VungleSDKBannerView
import com.vungle.ads.VungleError
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

class VungleBannerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    private var vungleSDKBanner: VungleSDKBannerView? = null
    private var bannerAd: BannerAd<VungleAdSize>? = null
    private var currentSize: VungleAdSize = VungleAdSize.BANNER

    val adSize: VungleAdSize get() = currentSize
    private var placementId: String? = null
    private var logger: AdLogger? = null
    private var reloadPolicies: Set<AdReloadPolicy> = emptySet()
    var retryRule: AdFailedRetryRule = exponentialDefault()

    private var keepAdSlot = true
    private var stateManager: AdStateManager? = null
    var adListener: BannerAdListener? = null

    init {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        if (attrs != null) {
            val a = context.obtainStyledAttributes(attrs, R.styleable.VungleBannerView)

            try {
                val reloadFlags = a.getInt(R.styleable.VungleBannerView_adReloadPolicies, 0)
                reloadPolicies = parseReloadPolicies(reloadFlags)
                keepAdSlot = a.getBoolean(R.styleable.VungleBannerView_keepAdSlot, true)
                val retryType = a.getInt(R.styleable.VungleBannerView_adFailedRetryRule, 3)

                val delay = a.getInt(
                    R.styleable.VungleBannerView_adFailedLoadDelayMillis,
                    AdFailedRetryRule.DEFAULT_DELAY_MS.toInt()
                ).toLong()

                val attempts = a.getInt(
                    R.styleable.VungleBannerView_adFailedLoadMaxAttempt,
                    AdFailedRetryRule.DEFAULT_MAX_ATTEMPTS
                )

                val multiplier = a.getFloat(
                    R.styleable.VungleBannerView_adFailedLoadMultiplier,
                    AdFailedRetryRule.DEFAULT_MULTIPLIER
                )

                retryRule = buildRetryRule(retryType, delay, attempts, multiplier)

                val sizeFlags = a.getInt(R.styleable.VungleBannerView_adFormats, 1)
                val loadType = a.getInt(R.styleable.VungleBannerView_adLoadType, 1)

                placementId = a.getString(R.styleable.VungleBannerView_placementId)

                val formats = parseFormats(sizeFlags)

                bannerAd = if (loadType == 2) {
                    BannerAd.Random(*formats.toTypedArray())
                } else {
                    BannerAd.Fixed(formats[0])
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

    private fun parseFormats(flags: Int): List<VungleAdSize> {
        val formats: MutableList<VungleAdSize> = mutableListOf()

        if ((flags and 1) != 0) formats.add(VungleAdSize.BANNER)
        if ((flags and 2) != 0) formats.add(VungleAdSize.MREC)
        if ((flags and 4) != 0) formats.add(VungleAdSize.BANNER_LEADERBOARD)

        if (formats.isEmpty()) {
            formats.add(VungleAdSize.BANNER)
        }

        return formats
    }

    fun loadAd() {
        if (stateManager == null) {
            stateManager = AdStateManager(reloadPolicies, retryRule, disable(), null, Scheduler(null), TAG) {
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
        destroyAd()
        currentSize = bannerAd?.getSize() ?: VungleAdSize.BANNER

        val finalPlacementId = if (testMode) TEST_AD_UNIT_ID else placementId!!
        vungleSDKBanner = VungleSDKBannerView(context, finalPlacementId, currentSize)
        
        val adListenerWrapper = object : BaseAdListener, com.vungle.ads.BannerAdListener {
            override fun onAdLoaded(baseAd: BaseAd) {
                log("Banner loaded")
                stateManager?.onAdLoaded()
                adListener?.onAdLoaded()
                fadeIn()
            }

            override fun onAdStart(baseAd: BaseAd) {}

            override fun onAdFailedToLoad(baseAd: BaseAd, adError: VungleError) {
                log("Load failed: ${adError.errorMessage}")
                val adError = VungleUtils.adErrorFrom(adError)
                stateManager?.onAdFailedToLoad(adError)
                adListener?.onAdFailedToLoad(adError)
                if (!keepAdSlot) visibility = GONE
            }

            override fun onAdFailedToPlay(
                baseAd: BaseAd,
                adError: VungleError
            ) {

            }

            override fun onAdClicked(baseAd: BaseAd) {
                log("Ad clicked")
                adListener?.onAdClicked()
                stateManager?.onAdClicked()
            }

            override fun onAdEnd(baseAd: BaseAd) {

            }

            override fun onAdLeftApplication(baseAd: BaseAd) {
            }

            override fun onAdImpression(baseAd: BaseAd) {
                log("Ad Showed")
                adListener?.onAdDisplayed()
                stateManager?.onAdDisplayed()
            }
        }

        vungleSDKBanner!!.adListener = adListenerWrapper

        // Set layout parameters
        val width = LayoutParams.MATCH_PARENT
        val heightDp = when (currentSize) {
            VungleAdSize.BANNER -> 50
            VungleAdSize.MREC -> 250
            VungleAdSize.BANNER_LEADERBOARD -> 90
            else -> 50
        }
        val heightPx = (heightDp * context.resources.displayMetrics.density).toInt()
        vungleSDKBanner!!.layoutParams = LayoutParams(width, heightPx)

        addView(vungleSDKBanner)
        vungleSDKBanner!!.load()

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
        if (vungleSDKBanner != null) {
            removeView(vungleSDKBanner)
            stateManager?.onDestroy()
            stateManager = null
            vungleSDKBanner = null
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

    fun setAdSize(size: VungleAdSize) {
        bannerAd = BannerAd.fixed(size)
    }

    fun setBannerAd(size: BannerAd<io.github.saifullah.nurani.ads.core.AdSize>) {
        bannerAd = size.mapToBannerAd {
            when {
                it.height >= 250 -> VungleAdSize.MREC
                it.height >= 90 -> VungleAdSize.BANNER_LEADERBOARD
                else -> VungleAdSize.BANNER
            }
        }
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

    private var testMode = false

    fun setTestModeEnabled(enabled: Boolean) {
        this.testMode = enabled
    }

    companion object {
        const val TEST_AD_UNIT_ID: String = "B1-5606155"
        const val TAG: String = "VungleBannerView"
    }
}
