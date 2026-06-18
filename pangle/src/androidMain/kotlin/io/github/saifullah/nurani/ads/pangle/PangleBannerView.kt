package io.github.saifullah.nurani.ads.pangle

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import com.bytedance.sdk.openadsdk.api.banner.PAGBannerAd
import com.bytedance.sdk.openadsdk.api.banner.PAGBannerAdInteractionListener
import com.bytedance.sdk.openadsdk.api.banner.PAGBannerAdLoadListener
import com.bytedance.sdk.openadsdk.api.banner.PAGBannerRequest
import com.bytedance.sdk.openadsdk.api.banner.PAGBannerSize
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

class PangleBannerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    private var mPAGBannerAd: PAGBannerAd? = null
    private var bannerAd: BannerAd<PAGBannerSize>? = null
    private var currentSize: PAGBannerSize = PAGBannerSize.BANNER_W_320_H_50

    val adSize: PAGBannerSize get() = currentSize
    private var adUnitId: String? = null
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
            val a = context.obtainStyledAttributes(attrs, R.styleable.PangleBannerView)

            try {
                val reloadFlags = a.getInt(R.styleable.PangleBannerView_adReloadPolicies, 0)
                reloadPolicies = parseReloadPolicies(reloadFlags)
                keepAdSlot = a.getBoolean(R.styleable.PangleBannerView_keepAdSlot, true)
                val retryType = a.getInt(R.styleable.PangleBannerView_adFailedRetryRule, 3)

                val delay = a.getInt(
                    R.styleable.PangleBannerView_adFailedLoadDelayMillis,
                    AdFailedRetryRule.DEFAULT_DELAY_MS.toInt()
                ).toLong()

                val attempts = a.getInt(
                    R.styleable.PangleBannerView_adFailedLoadMaxAttempt,
                    AdFailedRetryRule.DEFAULT_MAX_ATTEMPTS
                )

                val multiplier = a.getFloat(
                    R.styleable.PangleBannerView_adFailedLoadMultiplier,
                    AdFailedRetryRule.DEFAULT_MULTIPLIER
                )

                retryRule = buildRetryRule(retryType, delay, attempts, multiplier)

                val sizeFlags = a.getInt(R.styleable.PangleBannerView_adFormats, 1)
                val loadType = a.getInt(R.styleable.PangleBannerView_adLoadType, 1)

                adUnitId = a.getString(R.styleable.PangleBannerView_adUnitId)

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

    private fun parseSizes(flags: Int): List<PAGBannerSize> {
        val sizes: MutableList<PAGBannerSize> = mutableListOf()

        if ((flags and 1) != 0) sizes.add(PAGBannerSize.BANNER_W_320_H_50)
        if ((flags and 2) != 0) sizes.add(PAGBannerSize.BANNER_W_300_H_250)

        if (sizes.isEmpty()) {
            sizes.add(PAGBannerSize.BANNER_W_320_H_50)
        }

        return sizes
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
        val id = adUnitId
        if (!testMode) {
            checkNotNull(id) { "adUnitId must be set." }
            require(id.isNotEmpty()) { "adUnitId must not be empty." }
        }
        if (!keepAdSlot) {
            visibility = GONE
        }
        destroyAd()
        currentSize = bannerAd?.getSize() ?: PAGBannerSize.BANNER_W_320_H_50

        val finalAdUnitId = if (testMode) {
            if (currentSize == PAGBannerSize.BANNER_W_300_H_250) TEST_AD_UNIT_ID_300_250 else TEST_AD_UNIT_ID
        } else {
            id!!
        }

        val bannerRequest = PAGBannerRequest(currentSize)

        PAGBannerAd.loadAd(finalAdUnitId, bannerRequest, object : PAGBannerAdLoadListener {
            override fun onError(code: Int, message: String) {
                log("Load failed: code: $code, message: $message")
                val adError = PangleUtils.adErrorFrom(code, message)
                stateManager?.onAdFailedToLoad(adError)
                adListener?.onAdFailedToLoad(adError)
                if (!keepAdSlot) visibility = GONE
            }

            override fun onAdLoaded(ad: PAGBannerAd?) {
                if (ad == null) return
                log("Banner loaded")
                mPAGBannerAd = ad

                ad.setAdInteractionListener(object : PAGBannerAdInteractionListener {
                    override fun onAdShowed() {
                        log("Ad Showed")
                        adListener?.onAdDisplayed()
                        stateManager?.onAdDisplayed()
                    }

                    override fun onAdClicked() {
                        log("Ad clicked")
                        adListener?.onAdClicked()
                        stateManager?.onAdClicked()
                    }

                    override fun onAdDismissed() {
                        log("Ad Dismissed")
                        stateManager?.onAdDismissed()
                        adListener?.onAdDismissed()
                    }
                })

                val bannerView = ad.bannerView
                if (bannerView != null) {
                    val heightDp = when (currentSize) {
                        PAGBannerSize.BANNER_W_300_H_250 -> 250
                        else -> 50
                    }
                    val heightPx = (heightDp * context.resources.displayMetrics.density).toInt()
                    bannerView.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, heightPx)
                    addView(bannerView)
                }
                stateManager?.onAdLoaded()
                adListener?.onAdLoaded()
                fadeIn()
            }
        })

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
        if (mPAGBannerAd != null) {
            removeAllViews()
            stateManager?.onDestroy()
            stateManager = null
            mPAGBannerAd = null
        }
    }

    private fun log(msg: String?) {
        logger?.d("$TAG : $msg") ?: android.util.Log.d(TAG, msg ?: "")
    }

    fun setAdLogger(logger: AdLogger?) {
        this.logger = logger
    }

    fun setAdUnitId(id: String) {
        this.adUnitId = id
    }

    fun setAdSize(size: PAGBannerSize) {
        bannerAd = BannerAd.fixed(size)
    }

    fun setBannerAd(size: BannerAd<io.github.saifullah.nurani.ads.core.AdSize>) {
        bannerAd = size.mapToBannerAd {
            when {
                it.height >= 250 -> PAGBannerSize.BANNER_W_300_H_250
                else -> PAGBannerSize.BANNER_W_320_H_50
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
        const val TEST_AD_UNIT_ID: String = "980088185"
        const val TEST_AD_UNIT_ID_300_250: String = "980088184"
        const val TAG: String = "PangleBannerView"
    }
}
