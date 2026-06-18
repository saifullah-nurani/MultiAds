package io.github.saifullah.nurani.ads.applovin

import android.app.Activity
import android.content.Context
import android.os.Handler
import androidx.annotation.StringRes
import androidx.lifecycle.LifecycleOwner
import com.applovin.mediation.MaxAd
import com.applovin.mediation.MaxAdListener
import com.applovin.mediation.MaxError
import com.applovin.mediation.ads.MaxInterstitialAd
import io.github.saifullah.nurani.ads.core.AdConfig
import io.github.saifullah.nurani.ads.core.Scheduler
import io.github.saifullah.nurani.ads.core.utils.ContextUtils.Companion.findActivity

class AppLovinInterstitialAd(
    val context: Context,
    val adUnitId: String,
    adConfig: AdConfig?,
    handler: Handler?
) : FullScreenAdState(context, Scheduler(handler), adConfig, TAG) {
    private var mInterstitialAd: MaxInterstitialAd? = null

    private val interstitialAdListener = object : MaxAdListener {
        override fun onAdLoaded(ad: MaxAd) {
            adStateManager.onAdLoaded()
            adLoadListener?.onAdLoaded()
        }

        override fun onAdLoadFailed(adUnitId: String, error: MaxError) {
            val adError = AppLovinUtils.adErrorFrom(error)
            adStateManager.onAdFailedToLoad(adError)
            adLoadListener?.onAdFailedToLoad(adError)
            if (adStateManager.shouldPreserveOnFailure) {
                mInterstitialAd = null
            }
        }

        override fun onAdDisplayed(ad: MaxAd) {
            adStateManager.onAdShowed()
            adScreenContentCallback?.onAdShowed()
        }

        override fun onAdClicked(ad: MaxAd) {
            adStateManager.onAdClicked()
            adScreenContentCallback?.onAdClicked()
        }

        override fun onAdHidden(ad: MaxAd) {
            clean()
            adStateManager.onAdDismissed()
            adScreenContentCallback?.onAdDismissed()
        }

        override fun onAdDisplayFailed(ad: MaxAd, error: MaxError) {
            val adError = AppLovinUtils.adErrorFrom(error)
            adScreenContentCallback?.onAdFailedToShow(adError)
            clean()
        }
    }

    override val isAdAvailable: Boolean get() = mInterstitialAd != null && mInterstitialAd!!.isReady

    override fun loadAd() {
        if (mInterstitialAd != null && mInterstitialAd!!.isReady) return
        reloadAd()
    }

    override fun onAdLoad() {
        if (!adConfig.isTestModeEnabled) {
            checkNotNull(adUnitId) { "adUnitId must be set." }
            require(adUnitId.isNotEmpty()) { "adUnitId must not be empty." }
        }
        val activity = findActivity(context) ?: return
        mInterstitialAd?.destroy()
        
        val finalAdUnitId = if (adConfig.isTestModeEnabled) TEST_AD_UNIT_ID else adUnitId
        val ad = MaxInterstitialAd(finalAdUnitId, activity)
        mInterstitialAd = ad
        ad.setListener(interstitialAdListener)
        ad.loadAd()
    }

    override fun clean() {
        mInterstitialAd?.destroy()
        mInterstitialAd = null
    }

    override fun showAd(owner: Activity) {
        if (isAdAvailable) {
            mInterstitialAd!!.showAd()
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
        fun with(context: Context, adUnitId: String): AppLovinInterstitialAd {
            return AppLovinInterstitialAd(context, adUnitId, null, null)
        }

        @JvmStatic
        fun with(context: Context, @StringRes adUnitIdRes: Int): AppLovinInterstitialAd {
            return with(context, context.getString(adUnitIdRes))
        }

        const val TEST_AD_UNIT_ID: String = "YOUR_MAX_TEST_AD_UNIT_ID"
        const val TAG: String = "AppLovinInterstitialAd"
    }
}
