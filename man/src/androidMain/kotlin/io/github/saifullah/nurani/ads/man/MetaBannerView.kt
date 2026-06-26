package io.github.saifullah.nurani.ads.man

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import com.facebook.ads.Ad
import com.facebook.ads.AdError
import com.facebook.ads.AdListener
import com.facebook.ads.AdSize
import com.facebook.ads.AdView
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

class MetaBannerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    private var adView: AdView? = null
    private var bannerAd: BannerAd<AdSize>? = null
    private var currentAdSize: AdSize = AdSize.BANNER_HEIGHT_50

    val adSize: AdSize get() = currentAdSize
    private var placementId: String? = null
    private var logger: AdLogger? = null
    private var reloadPolicies: Set<AdReloadPolicy> = emptySet()
    var retryRule: AdFailedRetryRule = exponentialDefault()

    private var keepAdSlot = true
    private var testMode = false
    private var requestTag: String? = null

    private var stateManager: AdStateManager? = null
    var adListener: BannerAdListener? = null

    init {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        if (attrs != null) {
            val a = context.obtainStyledAttributes(attrs, R.styleable.MetaBannerView)

            try {
                val reloadFlags = a.getInt(R.styleable.MetaBannerView_adReloadPolicies, 0)
                reloadPolicies = parseReloadPolicies(reloadFlags)
                keepAdSlot = a.getBoolean(R.styleable.MetaBannerView_keepAdSlot, true)
                val retryType = a.getInt(R.styleable.MetaBannerView_adFailedRetryRule, 3)

                val delay = a.getInt(
                    R.styleable.MetaBannerView_adFailedLoadDelayMillis,
                    AdFailedRetryRule.DEFAULT_DELAY_MS.toInt()
                ).toLong()

                val attempts = a.getInt(
                    R.styleable.MetaBannerView_adFailedLoadMaxAttempt,
                    AdFailedRetryRule.DEFAULT_MAX_ATTEMPTS
                )

                val multiplier = a.getFloat(
                    R.styleable.MetaBannerView_adFailedLoadMultiplier,
                    AdFailedRetryRule.DEFAULT_MULTIPLIER
                )

                retryRule = buildRetryRule(retryType, delay, attempts, multiplier)

                val sizeFlags = a.getInt(R.styleable.MetaBannerView_adFormats, 1)
                val loadType = a.getInt(R.styleable.MetaBannerView_adLoadType, 1)

                placementId = a.getString(R.styleable.MetaBannerView_placementId)

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

        if ((flags and 1) != 0) sizes.add(AdSize.BANNER_HEIGHT_50)
        if ((flags and 2) != 0) sizes.add(AdSize.BANNER_HEIGHT_90)
        if ((flags and 4) != 0) sizes.add(AdSize.RECTANGLE_HEIGHT_250)

        if (sizes.isEmpty()) {
            sizes.add(AdSize.BANNER_HEIGHT_50)
        }

        return sizes
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
        if (!testMode) {
            checkNotNull(placementId) { "placementId must be set." }
            require(placementId!!.isNotEmpty()) { "placementId must not be empty." }
        }
        if (!keepAdSlot) {
            visibility = GONE
        }
        destroyAd()
        currentAdSize = bannerAd?.getSize() ?: AdSize.BANNER_HEIGHT_50

        val finalPlacementId = if (testMode) TEST_AD_UNIT_ID else placementId

        adView = AdView(context, finalPlacementId, currentAdSize)

        val adListenerWrapper = object : AdListener {
            override fun onError(ad: Ad?, adError: AdError?) {
                val msg = adError?.errorMessage ?: "Unknown error"
                log("Load failed: $msg")
                val error =
                    if (adError != null) MetaUtils.adErrorFrom(adError) else io.github.saifullah.nurani.ads.core.AdError(
                        -1,
                        "Unknown error"
                    )
                stateManager?.onAdFailedToLoad(error)
                adListener?.onAdFailedToLoad(error)
                if (!keepAdSlot) visibility = GONE
            }

            override fun onAdLoaded(ad: Ad?) {
                log("Banner loaded")
                stateManager?.onAdLoaded()
                adListener?.onAdLoaded()
                fadeIn()
            }

            override fun onAdClicked(ad: Ad?) {
                log("Ad clicked")
                adListener?.onAdClicked()
                stateManager?.onAdClicked()
            }

            override fun onLoggingImpression(ad: Ad?) {
                log("Ad Showed")
                adListener?.onAdDisplayed()
                stateManager?.onAdDisplayed()
            }
        }

        val config = adView!!.buildLoadAdConfig()
            .withAdListener(adListenerWrapper)
            .build()

        addView(adView)
        adView!!.loadAd(config)

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

    fun setRequestTag(tag: String?) {
        this.requestTag = tag
    }

    fun setPlacementId(id: String) {
        this.placementId = id
    }

    fun setAdSize(size: AdSize) {
        bannerAd = BannerAd.fixed(size)
    }

    fun setBannerAd(size: BannerAd<io.github.saifullah.nurani.ads.core.AdSize>) {
        bannerAd = size.mapToBannerAd {
            when {
                it.height >= 250 -> AdSize.RECTANGLE_HEIGHT_250
                it.height >= 90 -> AdSize.BANNER_HEIGHT_90
                else -> AdSize.BANNER_HEIGHT_50
            }
        }
    }

    fun setKeepAdSlot(keepAdSlot: Boolean) {
        this.keepAdSlot = keepAdSlot
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        destroyAd()
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
    }

    fun pause() {
    }

    fun resume() {
    }

    fun destroy() {
        destroyAd()
    }

    companion object {
        const val TEST_AD_UNIT_ID: String = "IMG_16_9_APP_INSTALL#YOUR_PLACEMENT_ID"
        const val TAG: String = "MetaBannerView"
    }
}
