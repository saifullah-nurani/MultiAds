package io.github.saifullah.nurani.ads.pangle

import android.app.Activity
import android.content.Context
import android.os.Handler
import androidx.annotation.StringRes
import androidx.lifecycle.LifecycleOwner
import com.bytedance.sdk.openadsdk.api.reward.PAGRewardItem
import com.bytedance.sdk.openadsdk.api.reward.PAGRewardedAd
import com.bytedance.sdk.openadsdk.api.reward.PAGRewardedAdInteractionListener
import com.bytedance.sdk.openadsdk.api.reward.PAGRewardedAdLoadListener
import com.bytedance.sdk.openadsdk.api.reward.PAGRewardedRequest
import io.github.saifullah.nurani.ads.core.AdConfig
import io.github.saifullah.nurani.ads.core.AdError
import io.github.saifullah.nurani.ads.core.OnUserRewardedListener
import io.github.saifullah.nurani.ads.core.Scheduler
import io.github.saifullah.nurani.ads.core.compose.PlatformActivity
import io.github.saifullah.nurani.ads.core.utils.ContextUtils.Companion.findActivity

class PangleRewardedAd(
    val context: Context,
    val adUnitId: String,
    adConfig: AdConfig?,
    handler: Handler?
) : RewardedAdState(context, Scheduler(handler), adConfig, TAG) {
    private var mRewarded: PAGRewardedAd? = null

    private val rewardedAdLoadListener = object : PAGRewardedAdLoadListener {
        override fun onError(code: Int, message: String) {
            val adError = PangleUtils.adErrorFrom(code, message)
            adStateManager.onAdFailedToLoad(adError)
            adLoadListener?.onAdFailedToLoad(adError)
            if (adStateManager.shouldPreserveOnFailure) {
                mRewarded = null
            }
        }

        override fun onAdLoaded(rewardedAd: PAGRewardedAd) {
            mRewarded = rewardedAd
            rewardedAd.setAdInteractionListener(object : PAGRewardedAdInteractionListener {
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

                override fun onUserEarnedReward(rewardItem: PAGRewardItem?) {
                    userRewardedCallback?.invoke()
                }

                override fun onUserEarnedRewardFail(errorCode: Int, errorMessage: String) {
                    adLogger?.e("$TAG: User earned reward fail - code: $errorCode, message: $errorMessage")
                }
            })
            adStateManager.onAdLoaded()
            adLoadListener?.onAdLoaded()
        }
    }

    override val isAdAvailable: Boolean get() = mRewarded != null

    override fun loadAd() {
        if (mRewarded != null) return
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
        val request = PAGRewardedRequest()
        PAGRewardedAd.loadAd(finalAdUnitId, request, rewardedAdLoadListener)
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
            mRewarded!!.show(owner)
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
        fun with(context: Context, adUnitId: String): PangleRewardedAd {
            return PangleRewardedAd(context, adUnitId, null, null)
        }

        @JvmStatic
        fun with(context: Context, @StringRes adUnitIdRes: Int): PangleRewardedAd {
            return with(context, context.getString(adUnitIdRes))
        }

        const val TEST_AD_UNIT_ID: String = "980088190"
        const val TAG: String = "PangleRewardedAd"
    }
}
