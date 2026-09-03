package io.github.saifullah.nurani.ads.ironsource

import android.app.Activity
import android.content.Context
import android.os.Handler
import androidx.annotation.StringRes
import androidx.lifecycle.LifecycleOwner
import com.unity3d.mediation.LevelPlay
import com.unity3d.mediation.LevelPlayAdError
import com.unity3d.mediation.LevelPlayAdInfo
import com.unity3d.mediation.rewarded.LevelPlayReward
import com.unity3d.mediation.rewarded.LevelPlayRewardedAd
import com.unity3d.mediation.rewarded.LevelPlayRewardedAdListener
import io.github.saifullah.nurani.ads.core.AdConfig
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

    private var rewardedAd: LevelPlayRewardedAd? = null

    private val rewardedVideoListener = object : LevelPlayRewardedAdListener {
        override fun onAdLoaded(adInfo: LevelPlayAdInfo) {
            adStateManager.onAdLoaded()
            adLoadListener?.onAdLoaded()
        }

        override fun onAdLoadFailed(error: LevelPlayAdError) {
            val adError = IronSourceUtils.adErrorFrom(error)
            adStateManager.onAdFailedToLoad(adError)
            adLoadListener?.onAdFailedToLoad(adError)
        }

        override fun onAdDisplayed(adInfo: LevelPlayAdInfo) {
            adStateManager.onAdShowed()
            adScreenContentCallback?.onAdShowed()
        }

        override fun onAdDisplayFailed(error: LevelPlayAdError, adInfo: LevelPlayAdInfo) {
            val adError = IronSourceUtils.adErrorFrom(error)
            clean()
            adStateManager.onAdFailedToShow(adError)
            adScreenContentCallback?.onAdFailedToShow(adError)
        }

        override fun onAdClicked(adInfo: LevelPlayAdInfo) {
            adStateManager.onAdClicked()
            adScreenContentCallback?.onAdClicked()
        }

        override fun onAdClosed(adInfo: LevelPlayAdInfo) {
            clean()
            adStateManager.onAdDismissed()
            adScreenContentCallback?.onAdDismissed()
        }

        override fun onAdRewarded(reward: LevelPlayReward, adInfo: LevelPlayAdInfo) {
            userRewardedCallback?.invoke()
        }
    }

    override val isAdAvailable: Boolean get() = rewardedAd?.isAdReady() ?: false

    override fun loadAd() {
        if (isAdAvailable) return
        reloadAd()
    }

    override fun onAdLoad() {
        if (!IronSourceAds.isInitialized()) {
            IronSourceAds.runWhenInitialized {
                onAdLoad()
            }
            return
        }
        if (adConfig.isTestModeEnabled) {
            LevelPlay.setAdaptersDebug(true)
        } else {
            checkNotNull(placementName) { "placementName / adUnitId must be set." }
            require(placementName.isNotEmpty()) { "placementName / adUnitId must not be empty." }
        }
        val finalAdUnitId = if (adConfig.isTestModeEnabled) TEST_AD_UNIT_ID else placementName!!
        val ad = LevelPlayRewardedAd(finalAdUnitId)
        ad.setListener(rewardedVideoListener)
        rewardedAd = ad
        ad.loadAd()
    }

    override fun clean() {
        // Handled per instance
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
        val ad = rewardedAd
        if (ad != null && ad.isAdReady()) {
            setOnUserRewarded(onUserRewarded)
            ad.showAd(owner)
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
        adStateManager.addLifecycleOwner(owner)
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
        const val TEST_AD_UNIT_ID: String = "2452nmjt1t4g9z33"
        const val TAG: String = "IronSourceRewardedAd"
    }
}
