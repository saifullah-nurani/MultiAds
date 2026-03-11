package io.github.saifullah.nurani.ads.admob

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
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

class AdmobBannerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    private var adView: AdView? = null
    private var bannerAd: BannerAd<AdSize>? = null
    private var currentAdSize: AdSize = AdSize.BANNER

    private var adRequest : AdRequest  = AdRequest.Builder().build()
    val adSize: AdSize get() = currentAdSize
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
            val a = context.obtainStyledAttributes(attrs, R.styleable.AdmobBannerView)

            try {
                val reloadFlags = a.getInt(R.styleable.AdmobBannerView_adReloadPolicies, 0)
                reloadPolicies = parseReloadPolicies(reloadFlags)
                keepAdSlot = a.getBoolean(R.styleable.AdmobBannerView_keepAdSlot, true)
                val retryType = a.getInt(R.styleable.AdmobBannerView_adFailedRetryRule, 2)

                val delay = a.getInt(
                    R.styleable.AdmobBannerView_adFailedLoadDelayMillis,
                    AdFailedRetryRule.DEFAULT_DELAY_MS.toInt()
                ).toLong()

                val attempts = a.getInt(
                    R.styleable.AdmobBannerView_adFailedLoadMaxAttempt,
                    AdFailedRetryRule.DEFAULT_MAX_ATTEMPTS
                )

                val multiplier = a.getFloat(
                    R.styleable.AdmobBannerView_adFailedLoadMultiplier,
                    AdFailedRetryRule.DEFAULT_MULTIPLIER
                )

                retryRule = buildRetryRule(retryType, delay, attempts, multiplier)

                val sizeFlags = a.getInt(R.styleable.AdmobBannerView_adFormats, 1)
                val loadType = a.getInt(R.styleable.AdmobBannerView_adLoadType, 1)

                adUnitId = a.getString(R.styleable.AdmobBannerView_adUnitId)

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
        if (!adUnitId.isNullOrEmpty()) loadAd()
        else if (testMode) loadAd()
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
        if ((flags and 8) != 0) sizes.add(AdSize.FULL_BANNER)
        if ((flags and 16) != 0) sizes.add(AdSize.LEADERBOARD)

        if (sizes.isEmpty()) {
            sizes.add(AdSize.BANNER)
        }

        return sizes
    }

    @JvmOverloads
    fun loadAd(request: AdRequest = AdRequest.Builder().build()) {
        this.adRequest =request
        if (stateManager == null) {
            stateManager = AdStateManager(reloadPolicies, retryRule, disable(), null, Scheduler(null), TAG) {
                loadAdInternally(context, adRequest)
            }
        }

        stateManager!!.loadAd()
    }

    @SuppressLint("MissingPermission")
    private fun loadAdInternally(context: Context, request: AdRequest) {
        if (!testMode) {
            checkNotNull(adUnitId) { "adUnitId must be set." }
            if (!keepAdSlot) {
                visibility = GONE
            }
        }
        destroyAd()
        currentAdSize = bannerAd?.getSize()!!
        adView = AdView(context)
        adView!!.adUnitId = (if (testMode) TEST_AD_UNIT_ID else adUnitId)!!
        adView!!.setAdSize(currentAdSize)
        adView!!.adListener = object : AdListener() {
            override fun onAdLoaded() {
                log("Banner loaded")
                stateManager?.onAdLoaded()
                adListener?.onAdLoaded()
                fadeIn()
            }

            override fun onAdFailedToLoad(error: LoadAdError) {
                log("Load failed " + error.message)
                val error = AdmobUtils.adErrorFrom(error)
                stateManager?.onAdFailedToLoad(error)
                adListener?.onAdFailedToLoad(error)
                if (!keepAdSlot) visibility = GONE

            }

            override fun onAdClicked() {
                log("Ad clicked")
                adListener?.onAdClicked()
                stateManager?.onAdClicked()
            }

            override fun onAdImpression() {
                log("Ad Showed")
                adListener?.onAdDisplayed()
                stateManager?.onAdDisplayed()
            }

            override fun onAdClosed() {
                log("Ad Dismissed")
                stateManager?.onAdDismissed()
                adListener?.onAdDismissed()
            }

        }

        addView(adView)
        adView!!.loadAd(request)

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
        if (adView != null) {
            removeView(adView)
            stateManager?.onDestroy()
            adView!!.destroy()
            stateManager = null
            adView = null
        }
    }

    private fun log(msg: String?) {
        logger?.d("$TAG : $msg")
    }

    fun setAdLogger(logger: AdLogger?) {
        this.logger = logger
    }

    fun setTestModeEnabled(enabled: Boolean) {
        this.testMode = enabled
    }

    fun setAdUnitId(id: String) {
        this.adUnitId = id
    }

    fun setAdSize(size: AdSize) {
        bannerAd = BannerAd.fixed(size)
    }

    fun setBannerAd(size: BannerAd<io.github.saifullah.nurani.ads.core.AdSize>) {
        bannerAd = size.mapToBannerAd {
            AdSize(it.width, it.height)
        }
    }

    fun setKeepAdSlot(keepAdSlot: Boolean) {
        this.keepAdSlot = keepAdSlot
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        if (adView != null) {
            adView!!.resume()
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        destroyAd()
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)

        if (adView == null) return

        if (visibility == VISIBLE) {
            adView!!.resume()
        } else {
            adView!!.pause()
        }
    }

    fun pause() {
        adView?.pause()
    }

    fun resume() {
        adView?.resume()
    }

    fun destroy() {
        adView?.destroy()
    }

    companion object {
        const val TEST_AD_UNIT_ID: String = "ca-app-pub-3940256099942544/2435281174"

        const val TAG: String = "AdmobBannerView"
    }
}