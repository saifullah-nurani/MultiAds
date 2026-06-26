package io.github.saifullah.nurani.ads.inmobi

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import com.inmobi.ads.AdMetaInfo
import com.inmobi.ads.InMobiAdRequestStatus
import com.inmobi.ads.InMobiBanner
import com.inmobi.ads.listeners.BannerAdEventListener
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

class InMobiBannerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    private var inmobiBanner: InMobiBanner? = null
    private var bannerAd: BannerAd<AdSize>? = null
    private var currentSize: AdSize = AdSize.BANNER

    val adSize: AdSize get() = currentSize
    private var placementId: Long = 0L
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
            val a = context.obtainStyledAttributes(attrs, R.styleable.InMobiBannerView)

            try {
                val reloadFlags = a.getInt(R.styleable.InMobiBannerView_adReloadPolicies, 0)
                reloadPolicies = parseReloadPolicies(reloadFlags)
                keepAdSlot = a.getBoolean(R.styleable.InMobiBannerView_keepAdSlot, true)
                val retryType = a.getInt(R.styleable.InMobiBannerView_adFailedRetryRule, 3)

                val delay = a.getInt(
                    R.styleable.InMobiBannerView_adFailedLoadDelayMillis,
                    AdFailedRetryRule.DEFAULT_DELAY_MS.toInt()
                ).toLong()

                val attempts = a.getInt(
                    R.styleable.InMobiBannerView_adFailedLoadMaxAttempt,
                    AdFailedRetryRule.DEFAULT_MAX_ATTEMPTS
                )

                val multiplier = a.getFloat(
                    R.styleable.InMobiBannerView_adFailedLoadMultiplier,
                    AdFailedRetryRule.DEFAULT_MULTIPLIER
                )

                retryRule = buildRetryRule(retryType, delay, attempts, multiplier)

                val sizeFlags = a.getInt(R.styleable.InMobiBannerView_adFormats, 1)
                val loadType = a.getInt(R.styleable.InMobiBannerView_adLoadType, 1)

                val rawPlacement = a.getString(R.styleable.InMobiBannerView_placementId)
                if (!rawPlacement.isNullOrEmpty()) {
                    placementId = rawPlacement.toLongOrNull() ?: 0L
                }

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
        if (placementId != 0L) loadAd()
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

    private fun parseFormats(flags: Int): List<AdSize> {
        val formats: MutableList<AdSize> = mutableListOf()

        if ((flags and 1) != 0) formats.add(AdSize.BANNER)
        if ((flags and 2) != 0) formats.add(AdSize.MEDIUM_RECTANGLE)
        if ((flags and 4) != 0) formats.add(AdSize.LEADERBOARD)

        if (formats.isEmpty()) {
            formats.add(AdSize.BANNER)
        }

        return formats
    }

    fun loadAd() {
        if (stateManager == null) {
            stateManager =
                AdStateManager(reloadPolicies, retryRule, disable(), null, Scheduler(null), requestTag ?: TAG) {
                    loadAdInternally(context)
                }
        }

        stateManager!!.loadAd()
    }

    @SuppressLint("MissingPermission")
    private fun loadAdInternally(context: Context) {
        if (!InMobiAds.isInitialized()) {
            val adError = io.github.saifullah.nurani.ads.core.AdError(
                code = 0,
                message = "InMobi SDK is not initialized yet."
            )
            stateManager?.onAdFailedToLoad(adError)
            adListener?.onAdFailedToLoad(adError)
            return
        }
        if (!testMode) {
            check(placementId != 0L) { "placementId must be set." }
        }
        if (!keepAdSlot) {
            visibility = GONE
        }
        destroyAd()
        currentSize = bannerAd?.getSize() ?: AdSize.BANNER

        val finalPlacementId = if (testMode) TEST_AD_UNIT_ID else placementId
        inmobiBanner = InMobiBanner(context, finalPlacementId)

        val adListenerWrapper = object : BannerAdEventListener() {
            override fun onAdLoadSucceeded(ad: InMobiBanner, adMetaInfo: AdMetaInfo) {
                log("Banner loaded")
                stateManager?.onAdLoaded()
                adListener?.onAdLoaded()
                fadeIn()
            }

            override fun onAdLoadFailed(ad: InMobiBanner, status: InMobiAdRequestStatus) {
                log("Load failed: ${status.message}")
                val adError = InMobiUtils.adErrorFrom(status)
                stateManager?.onAdFailedToLoad(adError)
                adListener?.onAdFailedToLoad(adError)
                if (!keepAdSlot) visibility = GONE
            }

            override fun onAdClicked(ad: InMobiBanner, params: Map<Any, Any>?) {
                log("Ad clicked")
                adListener?.onAdClicked()
                stateManager?.onAdClicked()
            }

            override fun onAdDisplayed(ad: InMobiBanner) {
                log("Ad Showed")
                adListener?.onAdDisplayed()
                stateManager?.onAdDisplayed()
            }

            override fun onAdDismissed(ad: InMobiBanner) {
                stateManager?.onAdDismissed()
            }
        }

        inmobiBanner!!.setListener(adListenerWrapper)

        // Set Banner Size
        inmobiBanner!!.setBannerSize(currentSize.width, currentSize.height)

        // Set layout parameters
        val density = context.resources.displayMetrics.density
        val widthPx =
            if (currentSize.width > 0) (currentSize.width * density).toInt() else LayoutParams.MATCH_PARENT
        val heightPx = (currentSize.height * density).toInt()
        inmobiBanner!!.layoutParams = LayoutParams(widthPx, heightPx)

        addView(inmobiBanner)
        inmobiBanner!!.load()

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
        if (inmobiBanner != null) {
            removeView(inmobiBanner)
            stateManager?.onDestroy()
            stateManager = null
            inmobiBanner = null
        }
    }

    private fun log(msg: String?) {
        logger?.d("$TAG : $msg")
    }

    fun setAdLogger(logger: AdLogger?) {
        this.logger = logger
    }

    fun setPlacementId(id: Long) {
        this.placementId = id
    }

    fun setAdSize(size: AdSize) {
        bannerAd = BannerAd.fixed(size)
    }

    fun setBannerAd(size: BannerAd<AdSize>) {
        bannerAd = size
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
    private var requestTag: String? = null

    fun setTestModeEnabled(enabled: Boolean) {
        this.testMode = enabled
    }

    fun setRequestTag(tag: String?) {
        this.requestTag = tag
    }

    companion object {
        const val TEST_AD_UNIT_ID: Long = 1234567890L
        const val TAG: String = "InMobiBannerView"
    }
}
