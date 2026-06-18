package io.github.saifullah.nurani.ads.`is`

import android.app.Activity
import android.content.Context
import android.os.Handler
import androidx.annotation.StringRes
import androidx.lifecycle.LifecycleOwner
import com.ironsource.mediationsdk.IronSource
import com.ironsource.mediationsdk.adunit.adapter.utility.AdInfo
import com.ironsource.mediationsdk.logger.IronSourceError
import com.ironsource.mediationsdk.model.Placement
import com.ironsource.mediationsdk.sdk.LevelPlayRewardedVideoListener
import io.github.saifullah.nurani.ads.core.AdConfig
import io.github.saifullah.nurani.ads.core.AdError
import io.github.saifullah.nurani.ads.core.OnUserRewardedListener
import io.github.saifullah.nurani.ads.core.Scheduler
import io.github.saifullah.nurani.ads.core.compose.PlatformActivity
import io.github.saifullah.nurani.ads.core.utils.ContextUtils.Companion.findActivity

class IronSourceRewardedAd(
    val context: Context,
    val placementName: String?,
    adConfig: AdConfig?,
    handler: Handler?
) : RewardedAdState(context, Scheduler(handler), adConfig, TAG) {

    private val rewardedVideoListener = object : LevelPlayRewardedVideoListener {
        override fun onAdAvailable(adInfo: AdInfo) {
            adStateManager.onAdLoaded()
            adLoadListener?.onAdLoaded()
        }

        override fun onAdUnavailable() {
            val adError = AdError(-1, "Rewarded ad unavailable", null, null)
            adStateManager.onAdFailedToLoad(adError)
            adLoadListener?.onAdFailedToLoad(adError)
        }

        override fun onAdOpened(adInfo: AdInfo) {
            adStateManager.onAdShowed()
            adScreenContentCallback?.onAdShowed()
        }

        override fun onAdShowFailed(error: IronSourceError, adInfo: AdInfo) {
            val adError = IronSourceUtils.adErrorFrom(error)
            adScreenContentCallback?.onAdFailedToShow(adError)
            clean()
        }

        override fun onAdClicked(placement: Placement?, adInfo: AdInfo) {
            adStateManager.onAdClicked()
            adScreenContentCallback?.onAdClicked()
        }

        override fun onAdClosed(adInfo: AdInfo) {
            clean()
            adStateManager.onAdDismissed()
            adScreenContentCallback?.onAdDismissed()
        }

        override fun onAdRewarded(placement: Placement?, adInfo: AdInfo) {
            userRewardedCallback?.invoke()
        }
    }

    override val isAdAvailable: Boolean get() = IronSource.isRewardedVideoAvailable()

    override fun loadAd() {
        if (isAdAvailable) return
        reloadAd()
    }

    override fun onAdLoad() {
        if (!adConfig.isTestModeEnabled) {
            checkNotNull(placementName) { "placementName must be set." }
            require(placementName.isNotEmpty()) { "placementName must not be empty." }
        }
        IronSource.setLevelPlayRewardedVideoListener(rewardedVideoListener)
        IronSource.loadRewardedVideo()
    }

    override fun clean() {
        // No explicit destroy needed, LevelPlay handles caching
    }

    override fun showAd(owner: Activity) {
        showAd(owner = owner, onUserRewarded = {})
    }

    fun showAd(activity: Activity, listener: OnUserRewardedListener) {
        showAd(activity) { listener.onUserRewarded() }
    }

    @Throws(IllegalStateException::class)
    fun showAdOrThrow(listener: OnUserRewardedListener) {
        val activity = findActivity(context)
        checkNotNull(activity) { "$TAG: Context is not an Activity." }

        check(isAdAvailable) { "$TAG: is not loaded." }
        showAd(activity, listener)
    }

    override fun showAd(onUserRewarded: () -> Unit) {
        tryShowAd(onUserRewarded)
    }

    override fun showAd(owner: PlatformActivity, onUserRewarded: () -> Unit) {
        if (isAdAvailable) {
            setOnUserRewarded(onUserRewarded)
            IronSource.setLevelPlayRewardedVideoListener(rewardedVideoListener)
            val finalPlacement = if (adConfig.isTestModeEnabled) TEST_AD_UNIT_ID else placementName
            if (finalPlacement != null) {
                IronSource.showRewardedVideo(finalPlacement)
            } else {
                IronSource.showRewardedVideo()
            }
        }
    }

    override fun tryShowAd(): Boolean {
        return tryShowAd {}
    }

    fun tryShowAd(listener: OnUserRewardedListener): Boolean {
        try {
            showAdOrThrow(listener)
            return true
        } catch (e: IllegalStateException) {
            adLogger?.e(e.message)
            return false
        }
    }

    fun tryShowAd(onUserRewarded: () -> Unit): Boolean {
        try {
            val activity = findActivity(context)
            checkNotNull(activity) { "$TAG: Context is not an Activity." }
            check(isAdAvailable) { "$TAG: is not loaded." }
            showAd(activity, onUserRewarded)
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
        fun with(context: Context, placementName: String?): IronSourceRewardedAd {
            return IronSourceRewardedAd(context, placementName, null, null)
        }

        @JvmStatic
        fun with(context: Context, @StringRes placementNameRes: Int): IronSourceRewardedAd {
            return with(context, context.getString(placementNameRes))
        }
        const val TEST_AD_UNIT_ID: String = "1hv15us4p1j74q7j"
        const val TAG: String = "IronSourceRewardedAd"
    }
}
