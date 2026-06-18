package io.github.saifullah.nurani.ads.applovin

import android.app.Activity
import android.content.Context
import android.os.Handler
import androidx.annotation.StringRes
import androidx.lifecycle.LifecycleOwner
import com.applovin.mediation.MaxAd
import com.applovin.mediation.MaxAdListener
import com.applovin.mediation.MaxError
import com.applovin.mediation.ads.MaxAppOpenAd as MaxGmsAppOpenAd
import io.github.saifullah.nurani.ads.core.AdConfig
import io.github.saifullah.nurani.ads.core.AppOpenAd
import io.github.saifullah.nurani.ads.core.Scheduler
import io.github.saifullah.nurani.ads.core.utils.ContextUtils.Companion.findActivity

class AppLovinAppOpenAd(
    val context: Context,
    val adUnitId: String,
    adConfig: AdConfig?,
    handler: Handler?
) : FullScreenAdState(context, Scheduler(handler), adConfig, TAG), AppOpenAd {
    private var mAppOpenAd: MaxGmsAppOpenAd? = null

    private val appOpenAdListener = object : MaxAdListener {
        override fun onAdLoaded(ad: MaxAd) {
            adStateManager.onAdLoaded()
            adLoadListener?.onAdLoaded()
        }

        override fun onAdLoadFailed(adUnitId: String, error: MaxError) {
            val adError = AppLovinUtils.adErrorFrom(error)
            adStateManager.onAdFailedToLoad(adError)
            adLoadListener?.onAdFailedToLoad(adError)
            if (adStateManager.shouldPreserveOnFailure) {
                mAppOpenAd = null
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

    override val isAdAvailable: Boolean get() = mAppOpenAd != null && mAppOpenAd!!.isReady

    override fun loadAd() {
        if (mAppOpenAd != null && mAppOpenAd!!.isReady) return
        reloadAd()
    }

    override fun onAdLoad() {
        if (!adConfig.isTestModeEnabled) {
            checkNotNull(adUnitId) { "adUnitId must be set." }
            require(adUnitId.isNotEmpty()) { "adUnitId must not be empty." }
        }
        val activity = findActivity(context) ?: return
        mAppOpenAd?.destroy()

        val finalAdUnitId = if (adConfig.isTestModeEnabled) TEST_AD_UNIT_ID else adUnitId
        val ad = MaxGmsAppOpenAd(finalAdUnitId, context)
        mAppOpenAd = ad
        ad.setListener(appOpenAdListener)
        ad.loadAd()
    }

    override fun clean() {
        mAppOpenAd?.destroy()
        mAppOpenAd = null
    }

    override fun showAd(owner: Activity) {
        if (isAdAvailable) {
            mAppOpenAd!!.showAd()
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
        return try {
            showAdOrThrow()
            true
        } catch (e: IllegalStateException) {
            adLogger?.e(e.message)
            false
        }
    }

    override fun addLifecycleOwner(owner: LifecycleOwner) {
        owner.lifecycle.addObserver(adStateManager)
    }

    companion object {
        @JvmStatic
        fun with(context: Context, adUnitId: String): AppLovinAppOpenAd {
            return AppLovinAppOpenAd(context, adUnitId, null, null)
        }

        @JvmStatic
        fun with(context: Context, @StringRes adUnitIdRes: Int): AppLovinAppOpenAd {
            return with(context, context.getString(adUnitIdRes))
        }

        const val TEST_AD_UNIT_ID: String = "YOUR_MAX_TEST_AD_UNIT_ID"
        const val TAG: String = "AppLovinAppOpenAd"
    }
}
