package io.github.saifullah.nurani.ads.pangle

import android.app.Activity
import android.content.Context
import android.os.Handler
import androidx.annotation.StringRes
import androidx.lifecycle.LifecycleOwner
import com.bytedance.sdk.openadsdk.api.interstitial.PAGInterstitialAd
import com.bytedance.sdk.openadsdk.api.interstitial.PAGInterstitialAdInteractionListener
import com.bytedance.sdk.openadsdk.api.interstitial.PAGInterstitialAdLoadListener
import com.bytedance.sdk.openadsdk.api.interstitial.PAGInterstitialRequest
import io.github.saifullah.nurani.ads.core.AdConfig
import io.github.saifullah.nurani.ads.core.AdError
import io.github.saifullah.nurani.ads.core.Scheduler
import io.github.saifullah.nurani.ads.core.utils.ContextUtils.Companion.findActivity

class PangleInterstitialAd(
    val context: Context,
    val adUnitId: String,
    adConfig: AdConfig?,
    handler: Handler?
) : FullScreenAdState(context, Scheduler(handler), adConfig, TAG) {
    private var mInterstitialAd: PAGInterstitialAd? = null

    private val interstitialAdLoadListener = object : PAGInterstitialAdLoadListener {
        override fun onError(code: Int, message: String) {
            val adError = PangleUtils.adErrorFrom(code, message)
            adStateManager.onAdFailedToLoad(adError)
            adLoadListener?.onAdFailedToLoad(adError)
            if (adStateManager.shouldPreserveOnFailure) {
                mInterstitialAd = null
            }
        }

        override fun onAdLoaded(interstitialAd: PAGInterstitialAd) {
            mInterstitialAd = interstitialAd
            interstitialAd.setAdInteractionListener(object : PAGInterstitialAdInteractionListener {
                override fun onAdShowed() {
                    adStateManager.onAdShowed()
                    adScreenContentCallback?.onAdShowed()
                }

                override fun onAdClicked() {
                    adStateManager.onAdClicked()
                    adScreenContentCallback?.onAdClicked()
                }

                override fun onAdDismissed() {
                    clean()
                    adStateManager.onAdDismissed()
                    adScreenContentCallback?.onAdDismissed()
                }
            })
            adStateManager.onAdLoaded()
            adLoadListener?.onAdLoaded()
        }
    }

    override val isAdAvailable: Boolean get() = mInterstitialAd != null

    override fun loadAd() {
        if (mInterstitialAd != null) return
        reloadAd()
    }

    override fun onAdLoad() {
        if (!PangleAds.isInitialized()) {
            val adError = AdError(
                code = 0,
                message = "Pangle SDK is not initialized yet."
            )
            adStateManager.onAdFailedToLoad(adError)
            adLoadListener?.onAdFailedToLoad(adError)
            return
        }
        if (!adConfig.isTestModeEnabled) {
            require(adUnitId.isNotEmpty()) { "$TAG: adUnitId must not be empty." }
        }
        val finalAdUnitId = if (adConfig.isTestModeEnabled) TEST_AD_UNIT_ID else adUnitId
        val request = PAGInterstitialRequest()
        PAGInterstitialAd.loadAd(finalAdUnitId, request, interstitialAdLoadListener)
    }

    override fun clean() {
        mInterstitialAd = null
    }

    override fun showAd(owner: Activity) {
        if (isAdAvailable) {
            mInterstitialAd!!.show(owner)
        }
    }

    @Throws(IllegalStateException::class)
    fun showAdOrThrow() {
        val activity = findActivity(context)
        checkNotNull(activity) { "$TAG: Context is not an Activity." }

        check(isAdAvailable) { "$TAG: is not loaded." }
        showAd(activity)
    }

    override fun tryShowAd(): Boolean {
        try {
            showAdOrThrow()
            return true
        } catch (e: IllegalStateException) {
            adLogger?.e(e.message)
            return false
        }
    }

    override fun addLifecycleOwner(owner: LifecycleOwner) {
        owner.lifecycle.addObserver(adStateManager)
    }

    companion object {
        @JvmStatic
        fun with(context: Context, adUnitId: String): PangleInterstitialAd {
            return PangleInterstitialAd(context, adUnitId, null, null)
        }

        @JvmStatic
        fun with(context: Context, @StringRes adUnitIdRes: Int): PangleInterstitialAd {
            return with(context, context.getString(adUnitIdRes))
        }

        const val TEST_AD_UNIT_ID: String = "980088186"
        const val TAG: String = "PangleInterstitialAd"
    }
}
