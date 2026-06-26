package io.github.saifullah.nurani.ads.ironsource

import android.app.Activity
import android.content.Context
import android.os.Handler
import androidx.annotation.StringRes
import androidx.lifecycle.LifecycleOwner
import com.ironsource.mediationsdk.IronSource
import com.ironsource.mediationsdk.adunit.adapter.utility.AdInfo
import com.ironsource.mediationsdk.logger.IronSourceError
import com.ironsource.mediationsdk.sdk.LevelPlayInterstitialListener
import io.github.saifullah.nurani.ads.core.AdConfig
import io.github.saifullah.nurani.ads.core.Scheduler
import io.github.saifullah.nurani.ads.core.utils.ContextUtils.Companion.findActivity

class IronSourceInterstitialAd(
    val context: Context,
    val placementName: String?,
    adConfig: AdConfig?,
    handler: Handler?
) : FullScreenAdState(context, Scheduler(handler), adConfig, TAG) {

    private val interstitialAdListener = object : LevelPlayInterstitialListener {
        override fun onAdReady(adInfo: AdInfo) {
            adStateManager.onAdLoaded()
            adLoadListener?.onAdLoaded()
        }

        override fun onAdLoadFailed(error: IronSourceError) {
            val adError = IronSourceUtils.adErrorFrom(error)
            adStateManager.onAdFailedToLoad(adError)
            adLoadListener?.onAdFailedToLoad(adError)
        }

        override fun onAdOpened(adInfo: AdInfo) {
            adStateManager.onAdShowed()
            adScreenContentCallback?.onAdShowed()
        }

        override fun onAdShowSucceeded(adInfo: AdInfo) {
        }

        override fun onAdShowFailed(error: IronSourceError, adInfo: AdInfo) {
            val adError = IronSourceUtils.adErrorFrom(error)
            adScreenContentCallback?.onAdFailedToShow(adError)
            clean()
        }

        override fun onAdClicked(adInfo: AdInfo) {
            adStateManager.onAdClicked()
            adScreenContentCallback?.onAdClicked()
        }

        override fun onAdClosed(adInfo: AdInfo) {
            clean()
            adStateManager.onAdDismissed()
            adScreenContentCallback?.onAdDismissed()
        }
    }

    override val isAdAvailable: Boolean get() = IronSource.isInterstitialReady()

    override fun loadAd() {
        reloadAd()
    }

    override fun onAdLoad() {
        if (!adConfig.isTestModeEnabled) {
            checkNotNull(placementName) { "placementName must be set." }
            require(placementName.isNotEmpty()) { "placementName must not be empty." }
        }
        IronSource.setLevelPlayInterstitialListener(interstitialAdListener)
        IronSource.loadInterstitial()
    }

    override fun clean() {
        // No explicit destroy needed, global SDK caching handles instances
    }

    override fun showAd(owner: Activity) {
        if (isAdAvailable) {
            IronSource.setLevelPlayInterstitialListener(interstitialAdListener)
            val finalPlacement = if (adConfig.isTestModeEnabled) TEST_AD_UNIT_ID else placementName
            if (finalPlacement != null) {
                IronSource.showInterstitial(finalPlacement)
            } else {
                IronSource.showInterstitial()
            }
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
        fun with(context: Context, placementName: String?): IronSourceInterstitialAd {
            return IronSourceInterstitialAd(context, placementName, null, null)
        }

        @JvmStatic
        fun with(context: Context, @StringRes placementNameRes: Int): IronSourceInterstitialAd {
            return with(context, context.getString(placementNameRes))
        }

        const val TEST_AD_UNIT_ID: String = "re3gip7b41tqb2tm"
        const val TAG: String = "IronSourceInterstitialAd"
    }
}
