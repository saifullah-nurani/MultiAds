package io.github.saifullah.nurani.ads.admob

import android.app.Activity
import android.content.Context
import android.os.Handler
import androidx.annotation.StringRes
import androidx.lifecycle.LifecycleOwner
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.OnUserEarnedRewardListener
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import io.github.saifullah.nurani.ads.core.AdConfig
import io.github.saifullah.nurani.ads.core.OnUserRewardedListener
import io.github.saifullah.nurani.ads.core.Scheduler
import io.github.saifullah.nurani.ads.core.compose.PlatformActivity
import io.github.saifullah.nurani.ads.core.utils.ContextUtils.Companion.findActivity

class AdmobRewardedAd(
    val context: Context,
    adUnitId: String,
    adConfig: AdConfig?,
    adRequest: AdRequest?,
    handler: Handler?
) : RewardedAdState(context, Scheduler(handler), adConfig, TAG) {
    private var mRewarded: RewardedAd? = null
    private val adUnitId: String =
        (if (this.adConfig.isTestModeEnabled) TEST_AD_UNIT_ID else adUnitId)
    private val adRequest: AdRequest = adRequest ?: AdRequest.Builder().build()

    private val rewardedAdLoadCallback: RewardedAdLoadCallback =
        object : RewardedAdLoadCallback() {
            override fun onAdLoaded(rewardedAd: RewardedAd) {
                adStateManager.onAdLoaded()
                adLoadListener?.onAdLoaded()
                mRewarded = rewardedAd
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                val adError = AdmobUtils.adErrorFrom(loadAdError)
                adStateManager.onAdFailedToLoad(adError)
                adLoadListener?.onAdFailedToLoad(adError)
                if (adStateManager.shouldPreserveOnFailure) {
                    mRewarded = null
                }
            }
        }


    override val isAdAvailable: Boolean get() = mRewarded != null

    override fun loadAd() {
        if (mRewarded != null) return
        reloadAd()
    }


    override fun onAdLoad() {
        RewardedAd.load(context, adUnitId, adRequest, rewardedAdLoadCallback)
    }

    override fun clean() {
        mRewarded = null
    }

    override fun showAd(owner: Activity) {
        showAd(owner = owner, onUserRewarded = {})
    }


    fun showAd(activity: Activity, listener: OnUserEarnedRewardListener) {
        if (isAdAvailable) {
            mRewarded!!.setImmersiveMode(isImmersiveModeEnabled)
            mRewarded!!.fullScreenContentCallback =
                FullScreenContentCallback(adStateManager, adScreenContentCallback, ::clean)
            mRewarded!!.show(activity) {
                listener.onUserEarnedReward(it)
                userRewardedCallback?.invoke()
            }
        }
    }

    @Throws(IllegalStateException::class)
    fun showAdOrThrow(listener: OnUserRewardedListener) {
        showAdOrThrow { _: RewardItem? -> listener.onUserRewarded() }
    }

    @Throws(IllegalStateException::class)
    fun showAdOrThrow(listener: OnUserEarnedRewardListener) {
        val activity = findActivity(context)
        checkNotNull(activity) { "${TAG}: Context is not an Activity." }

        check(isAdAvailable) { "${TAG}: is not loaded." }
        showAd(activity, listener)
    }

    @JvmSynthetic
    override fun showAd(onUserRewarded: () -> Unit) {
        tryShowAd { _: RewardItem? -> onUserRewarded() }
    }

    @JvmSynthetic
    override fun showAd(
        owner: PlatformActivity,
        onUserRewarded: () -> Unit
    ) {
        showAd(owner, listener = OnUserEarnedRewardListener { onUserRewarded() })
    }

    override fun tryShowAd(): Boolean {
        return tryShowAd {}
    }

    fun tryShowAd(listener: OnUserRewardedListener): Boolean {
        return tryShowAd { _: RewardItem? -> listener.onUserRewarded() }
    }

    fun tryShowAd(listener: OnUserEarnedRewardListener): Boolean {
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

    override fun addLifecycleOwner(owner: LifecycleOwner) {
        owner.lifecycle.addObserver(adStateManager)
    }

    companion object {

        @JvmStatic
        fun with(context: Context, adUnitId: String): AdmobRewardedAd {
            return AdmobRewardedAd(context, adUnitId, null, null, null)
        }

        @JvmStatic
        fun with(context: Context, @StringRes adUnitId: Int): AdmobRewardedAd {
            return with(context, context.getString(adUnitId))
        }

        const val TAG: String = "AdmobRewardedAd"
        const val TEST_AD_UNIT_ID: String = "ca-app-pub-3940256099942544/5224354917"
    }
}
