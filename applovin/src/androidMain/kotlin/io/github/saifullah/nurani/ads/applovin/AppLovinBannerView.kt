package io.github.saifullah.nurani.ads.applovin

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import com.applovin.mediation.MaxAd
import com.applovin.mediation.MaxAdFormat
import com.applovin.mediation.MaxAdViewAdListener
import com.applovin.mediation.MaxError
import com.applovin.mediation.ads.MaxAdView
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

class AppLovinBannerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    private var maxAdView: MaxAdView? = null
    private var bannerAd: BannerAd<MaxAdFormat>? = null
    private var currentFormat: MaxAdFormat = MaxAdFormat.BANNER

    val adFormat: MaxAdFormat get() = currentFormat
    private var adUnitId: String? = null
    private var logger: AdLogger? = null
    private var reloadPolicies: Set<AdReloadPolicy> = emptySet()
    var retryRule: AdFailedRetryRule = exponentialDefault()

    private var keepAdSlot = true
    private var testMode = false

    private var stateManager: AdStateManager? = null
    var adListener: BannerAdListener? = null

    init {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        if (attrs != null) {
            val a = context.obtainStyledAttributes(attrs, R.styleable.AppLovinBannerView)

            try {
                val reloadFlags = a.getInt(R.styleable.AppLovinBannerView_adReloadPolicies, 0)
                reloadPolicies = parseReloadPolicies(reloadFlags)
                keepAdSlot = a.getBoolean(R.styleable.AppLovinBannerView_keepAdSlot, true)
                val retryType = a.getInt(R.styleable.AppLovinBannerView_adFailedRetryRule, 3)

                val delay = a.getInt(
                    R.styleable.AppLovinBannerView_adFailedLoadDelayMillis,
                    AdFailedRetryRule.DEFAULT_DELAY_MS.toInt()
                ).toLong()

                val attempts = a.getInt(
                    R.styleable.AppLovinBannerView_adFailedLoadMaxAttempt,
                    AdFailedRetryRule.DEFAULT_MAX_ATTEMPTS
                )

                val multiplier = a.getFloat(
                    R.styleable.AppLovinBannerView_adFailedLoadMultiplier,
                    AdFailedRetryRule.DEFAULT_MULTIPLIER
                )

                retryRule = buildRetryRule(retryType, delay, attempts, multiplier)

                val sizeFlags = a.getInt(R.styleable.AppLovinBannerView_adFormats, 1)
                val loadType = a.getInt(R.styleable.AppLovinBannerView_adLoadType, 1)

                adUnitId = a.getString(R.styleable.AppLovinBannerView_adUnitId)

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
        if (!adUnitId.isNullOrEmpty()) loadAd()
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

    private fun parseFormats(flags: Int): List<MaxAdFormat> {
        val formats: MutableList<MaxAdFormat> = mutableListOf()

        if ((flags and 1) != 0) formats.add(MaxAdFormat.BANNER)
        if ((flags and 2) != 0) formats.add(MaxAdFormat.MREC)
        if ((flags and 4) != 0) formats.add(MaxAdFormat.LEADER)

        if (formats.isEmpty()) {
            formats.add(MaxAdFormat.BANNER)
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
            checkNotNull(adUnitId) { "adUnitId must be set." }
            require(adUnitId!!.isNotEmpty()) { "adUnitId must not be empty." }
        }
        if (!keepAdSlot) {
            visibility = GONE
        }
        destroyAd()
        currentFormat = bannerAd?.getSize() ?: MaxAdFormat.BANNER

        val finalAdUnitId = if (testMode) TEST_AD_UNIT_ID else adUnitId!!
        maxAdView = MaxAdView(finalAdUnitId, currentFormat, context)
        
        val adListenerWrapper = object : MaxAdViewAdListener {
            override fun onAdLoaded(ad: MaxAd) {
                log("Banner loaded")
                stateManager?.onAdLoaded()
                adListener?.onAdLoaded()
                fadeIn()
            }

            override fun onAdLoadFailed(adUnitId: String, error: MaxError) {
                val msg = error.message ?: "Unknown error"
                log("Load failed: $msg")
                val adError = AppLovinUtils.adErrorFrom(error)
                stateManager?.onAdFailedToLoad(adError)
                adListener?.onAdFailedToLoad(adError)
                if (!keepAdSlot) visibility = GONE
            }

            override fun onAdClicked(ad: MaxAd) {
                log("Ad clicked")
                adListener?.onAdClicked()
                stateManager?.onAdClicked()
            }


            override fun onAdDisplayed(p0: MaxAd) {
                log("Ad Showed")
                adListener?.onAdDisplayed()
                stateManager?.onAdDisplayed()
            }

            override fun onAdHidden(p0: MaxAd) {
                stateManager?.onAdDismissed()
            }

            override fun onAdDisplayFailed(
                p0: MaxAd,
                p1: MaxError
            ) {
                val adError = AppLovinUtils.adErrorFrom(p1)
                adListener?.onAdFailedToLoad(adError)
            }

            override fun onAdExpanded(p0: MaxAd) {}
            override fun onAdCollapsed(p0: MaxAd) {}

        }

        maxAdView!!.setListener(adListenerWrapper)
        
        // Add layout params matching AppLovin requirements
        val width = LayoutParams.MATCH_PARENT
        val heightDp = currentFormat.getAdaptiveSize(context).height
        val heightPx = (heightDp * context.resources.displayMetrics.density).toInt()
        maxAdView!!.layoutParams = LayoutParams(width, heightPx)

        addView(maxAdView)
        maxAdView!!.loadAd()

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
        if (maxAdView != null) {
            removeView(maxAdView)
            stateManager?.onDestroy()
            maxAdView!!.destroy()
            stateManager = null
            maxAdView = null
        }
    }

    private fun log(msg: String?) {
        logger?.d("$TAG : $msg")
    }

    fun setAdLogger(logger: AdLogger?) {
        this.logger = logger
    }

    fun setAdUnitId(id: String) {
        this.adUnitId = id
    }

    fun setAdFormat(format: MaxAdFormat) {
        bannerAd = BannerAd.fixed(format)
    }

    fun setBannerAd(size: BannerAd<io.github.saifullah.nurani.ads.core.AdSize>) {
        bannerAd = size.mapToBannerAd {
            when {
                it.height >= 250 -> MaxAdFormat.MREC
                it.height >= 90 -> MaxAdFormat.LEADER
                else -> MaxAdFormat.BANNER
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

    fun setTestModeEnabled(enabled: Boolean) {
        this.testMode = enabled
    }

    companion object {
        const val TEST_AD_UNIT_ID: String = "YOUR_MAX_TEST_AD_UNIT_ID"
        const val TAG: String = "AppLovinBannerView"
    }
}
