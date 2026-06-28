package io.github.saifullah.nurani.ads.inmobi

import android.app.Activity
import android.content.Context
import android.os.Handler
import androidx.annotation.StringRes
import androidx.lifecycle.LifecycleOwner
import com.inmobi.ads.AdMetaInfo
import com.inmobi.ads.InMobiAdRequestStatus
import com.inmobi.ads.InMobiInterstitial
import com.inmobi.ads.listeners.InterstitialAdEventListener
import io.github.saifullah.nurani.ads.core.AdConfig
import io.github.saifullah.nurani.ads.core.AdError
import io.github.saifullah.nurani.ads.core.Scheduler
import io.github.saifullah.nurani.ads.core.utils.ContextUtils.Companion.findActivity

class InMobiInterstitialAd(
    val context: Context,
    val placementId: Long,
    adConfig: AdConfig?,
    handler: Handler?
) : FullScreenAdState(context, Scheduler(handler), adConfig, TAG) {
    private var mInterstitialAd: InMobiInterstitial? = null

    private val interstitialAdListener = object : InterstitialAdEventListener() {
        override fun onAdLoadSucceeded(ad: InMobiInterstitial, adMetaInfo: AdMetaInfo) {
            adStateManager.onAdLoaded()
            adLoadListener?.onAdLoaded()
        }

        override fun onAdLoadFailed(ad: InMobiInterstitial, status: InMobiAdRequestStatus) {
            val adError = InMobiUtils.adErrorFrom(status)
            adStateManager.onAdFailedToLoad(adError)
            adLoadListener?.onAdFailedToLoad(adError)
            if (adStateManager.shouldPreserveOnFailure) {
                mInterstitialAd = null
            }
        }

        override fun onAdDisplayed(ad: InMobiInterstitial, adMetaInfo: AdMetaInfo) {
            adStateManager.onAdShowed()
            adScreenContentCallback?.onAdShowed()
        }

        override fun onAdClicked(ad: InMobiInterstitial, params: Map<Any, Any>?) {
            adStateManager.onAdClicked()
            adScreenContentCallback?.onAdClicked()
        }

        override fun onAdDismissed(ad: InMobiInterstitial) {
            clean()
            adStateManager.onAdDismissed()
            adScreenContentCallback?.onAdDismissed()
        }

        override fun onAdDisplayFailed(ad: InMobiInterstitial) {
            val adError = AdError(-1, "Ad Display Failed")
            adScreenContentCallback?.onAdFailedToShow(adError)
            clean()
        }
    }

    override val isAdAvailable: Boolean get() = mInterstitialAd != null && mInterstitialAd!!.isReady()

    override fun loadAd() {
        if (mInterstitialAd != null && mInterstitialAd!!.isReady()) return
        reloadAd()
    }

    override fun onAdLoad() {
        if (!InMobiAds.isInitialized()) {
            val adError = AdError(
                code = 0,
                message = "InMobi SDK is not initialized yet."
            )
            adStateManager.onAdFailedToLoad(adError)
            adLoadListener?.onAdFailedToLoad(adError)
            return
        }
        if (!adConfig.isTestModeEnabled) {
            checkNotNull(placementId) { "placementId must be set." }
            require(placementId.toString().isNotEmpty()) { "placementId must not be empty." }
        }
        val finalPlacementId = if (adConfig.isTestModeEnabled) TEST_AD_UNIT_ID else placementId
        mInterstitialAd = InMobiInterstitial(context, finalPlacementId, interstitialAdListener)
        mInterstitialAd!!.load()
    }

    override fun clean() {
        mInterstitialAd = null
    }

    override fun showAd(owner: Activity) {
        if (isAdAvailable) {
            mInterstitialAd!!.show()
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
            if (adLogger != null) {
                adLogger!!.e(e.message)
            }
            return false
        }
    }

    override fun addLifecycleOwner(owner: LifecycleOwner) {
        owner.lifecycle.addObserver(adStateManager)
    }

    companion object {
        @JvmStatic
        fun with(context: Context, placementId: Long): InMobiInterstitialAd {
            return InMobiInterstitialAd(context, placementId, null, null)
        }
        const val TEST_AD_UNIT_ID: Long = 1234567890L
        const val TAG: String = "InMobiInterstitialAd"
    }
}
