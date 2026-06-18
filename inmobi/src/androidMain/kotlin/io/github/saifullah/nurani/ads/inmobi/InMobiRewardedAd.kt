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
import io.github.saifullah.nurani.ads.core.OnUserRewardedListener
import io.github.saifullah.nurani.ads.core.Scheduler
import io.github.saifullah.nurani.ads.core.compose.PlatformActivity
import io.github.saifullah.nurani.ads.core.utils.ContextUtils.Companion.findActivity

class InMobiRewardedAd(
    val context: Context,
    val placementId: Long,
    adConfig: AdConfig?,
    handler: Handler?
) : RewardedAdState(context, Scheduler(handler), adConfig, TAG) {
    private var mRewarded: InMobiInterstitial? = null

    private val rewardedAdListener = object : InterstitialAdEventListener() {
        override fun onAdLoadSucceeded(ad: InMobiInterstitial, adMetaInfo: AdMetaInfo) {
            adStateManager.onAdLoaded()
            adLoadListener?.onAdLoaded()
        }

        override fun onAdLoadFailed(ad: InMobiInterstitial, status: InMobiAdRequestStatus) {
            val adError = InMobiUtils.adErrorFrom(status)
            adStateManager.onAdFailedToLoad(adError)
            adLoadListener?.onAdFailedToLoad(adError)
            if (adStateManager.shouldPreserveOnFailure) {
                mRewarded = null
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
            val adError = io.github.saifullah.nurani.ads.core.AdError(-1, "Ad Display Failed")
            adScreenContentCallback?.onAdFailedToShow(adError)
            clean()
        }

        override fun onRewardsUnlocked(ad: InMobiInterstitial, rewards: Map<Any, Any>?) {
            userRewardedCallback?.invoke()
        }
    }

    override val isAdAvailable: Boolean get() = mRewarded != null && mRewarded!!.isReady()

    override fun loadAd() {
        if (mRewarded != null && mRewarded!!.isReady()) return
        reloadAd()
    }

    override fun onAdLoad() {
        if (!adConfig.isTestModeEnabled) {
            check(placementId != 0L) { "$TAG: placementId must be set." }
        }
        val finalPlacementId = if (adConfig.isTestModeEnabled) TEST_AD_UNIT_ID else placementId
        mRewarded = InMobiInterstitial(context, finalPlacementId, rewardedAdListener)
        mRewarded!!.load()
    }

    override fun clean() {
        mRewarded = null
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
            mRewarded!!.show()
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
            if (adLogger != null) {
                adLogger!!.e(e.message)
            }
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
        fun with(context: Context, placementId: Long): InMobiRewardedAd {
            return InMobiRewardedAd(context, placementId, null, null)
        }

        const val TEST_AD_UNIT_ID: Long = 1234567890L
        const val TAG: String = "InMobiRewardedAd"
    }
}
