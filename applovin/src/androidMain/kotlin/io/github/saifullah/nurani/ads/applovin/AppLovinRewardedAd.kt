package io.github.saifullah.nurani.ads.applovin

import android.app.Activity
import android.content.Context
import android.os.Handler
import androidx.annotation.StringRes
import androidx.lifecycle.LifecycleOwner
import com.applovin.mediation.MaxAd
import com.applovin.mediation.MaxError
import com.applovin.mediation.MaxReward
import com.applovin.mediation.MaxRewardedAdListener
import com.applovin.mediation.ads.MaxRewardedAd
import io.github.saifullah.nurani.ads.core.AdConfig
import io.github.saifullah.nurani.ads.core.OnUserRewardedListener
import io.github.saifullah.nurani.ads.core.Scheduler
import io.github.saifullah.nurani.ads.core.compose.PlatformActivity
import io.github.saifullah.nurani.ads.core.utils.ContextUtils.Companion.findActivity

class AppLovinRewardedAd(
    val context: Context,
    val adUnitId: String,
    adConfig: AdConfig?,
    handler: Handler?
) : RewardedAdState(context, Scheduler(handler), adConfig, TAG) {
    private var mRewarded: MaxRewardedAd? = null

    private val rewardedAdListener = object : MaxRewardedAdListener {
        override fun onAdLoaded(ad: MaxAd) {
            adStateManager.onAdLoaded()
            adLoadListener?.onAdLoaded()
        }

        override fun onAdLoadFailed(adUnitId: String, error: MaxError) {
            val adError = AppLovinUtils.adErrorFrom(error)
            adStateManager.onAdFailedToLoad(adError)
            adLoadListener?.onAdFailedToLoad(adError)
            if (adStateManager.shouldPreserveOnFailure) {
                mRewarded = null
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

        override fun onUserRewarded(ad: MaxAd, reward: MaxReward) {
            userRewardedCallback?.invoke()
        }
    }

    override val isAdAvailable: Boolean get() = mRewarded != null && mRewarded!!.isReady

    override fun loadAd() {
        if (mRewarded != null && mRewarded!!.isReady) return
        reloadAd()
    }

    override fun onAdLoad() {
        if (!adConfig.isTestModeEnabled) {
            checkNotNull(adUnitId) { "adUnitId must be set." }
            require(adUnitId.isNotEmpty()) { "adUnitId must not be empty." }
        }
        val activity = findActivity(context) ?: return
        mRewarded?.destroy()

        val finalAdUnitId = if (adConfig.isTestModeEnabled) TEST_AD_UNIT_ID else adUnitId
        val ad = MaxRewardedAd.getInstance(finalAdUnitId, activity)
        mRewarded = ad
        ad.setListener(rewardedAdListener)
        ad.loadAd()
    }

    override fun clean() {
        mRewarded?.destroy()
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
            mRewarded!!.showAd()
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
        fun with(context: Context, adUnitId: String): AppLovinRewardedAd {
            return AppLovinRewardedAd(context, adUnitId, null, null)
        }

        @JvmStatic
        fun with(context: Context, @StringRes adUnitIdRes: Int): AppLovinRewardedAd {
            return with(context, context.getString(adUnitIdRes))
        }

        const val TEST_AD_UNIT_ID: String = "YOUR_MAX_TEST_AD_UNIT_ID"
        const val TAG: String = "AppLovinRewardedAd"
    }
}
