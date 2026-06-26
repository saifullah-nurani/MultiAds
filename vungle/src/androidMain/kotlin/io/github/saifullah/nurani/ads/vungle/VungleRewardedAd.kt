package io.github.saifullah.nurani.ads.vungle

import android.app.Activity
import android.content.Context
import android.os.Handler
import androidx.annotation.StringRes
import androidx.lifecycle.LifecycleOwner
import com.vungle.ads.BaseAd
import com.vungle.ads.RewardedAd
import com.vungle.ads.RewardedAdListener
import com.vungle.ads.VungleError
import io.github.saifullah.nurani.ads.core.AdConfig
import io.github.saifullah.nurani.ads.core.OnUserRewardedListener
import io.github.saifullah.nurani.ads.core.Scheduler
import io.github.saifullah.nurani.ads.core.compose.PlatformActivity
import io.github.saifullah.nurani.ads.core.utils.ContextUtils.Companion.findActivity

class VungleRewardedAd(
    val context: Context,
    val placementId: String,
    adConfig: AdConfig?,
    handler: Handler?
) : RewardedAdState(context, Scheduler(handler), adConfig, TAG) {
    private var mRewarded: RewardedAd? = null

    private val rewardedAdListener = object : RewardedAdListener {


        override fun onAdClicked(baseAd: BaseAd) {
            adStateManager.onAdClicked()
            adScreenContentCallback?.onAdClicked()
        }

        override fun onAdEnd(baseAd: BaseAd) {
            clean()
            adStateManager.onAdDismissed()
            adScreenContentCallback?.onAdDismissed()
        }


        override fun onAdFailedToLoad(
            baseAd: BaseAd,
            adError: VungleError
        ) {
            val adError = VungleUtils.adErrorFrom(adError)
            adStateManager.onAdFailedToLoad(adError)
            adLoadListener?.onAdFailedToLoad(adError)
            if (adStateManager.shouldPreserveOnFailure) {
                mRewarded = null
            }
        }

        override fun onAdFailedToPlay(
            baseAd: BaseAd,
            adError: VungleError
        ) {
            val adError = VungleUtils.adErrorFrom(adError)
            adScreenContentCallback?.onAdFailedToShow(adError)
            clean()
        }

        override fun onAdImpression(baseAd: BaseAd) {

        }

        override fun onAdLeftApplication(baseAd: BaseAd) {

        }

        override fun onAdLoaded(baseAd: BaseAd) {
            adStateManager.onAdLoaded()
            adLoadListener?.onAdLoaded()
        }

        override fun onAdStart(baseAd: BaseAd) {
            adStateManager.onAdShowed()
            adScreenContentCallback?.onAdShowed()
        }

        override fun onAdRewarded(baseAd: BaseAd) {
            userRewardedCallback?.invoke()
        }
    }

    override val isAdAvailable: Boolean get() = mRewarded != null && mRewarded!!.canPlayAd()

    override fun loadAd() {
        if (mRewarded != null && mRewarded!!.canPlayAd()) return
        reloadAd()
    }

    override fun onAdLoad() {
        if (!VungleAds.isInitialized()) {
            val adError = io.github.saifullah.nurani.ads.core.AdError(
                code = 0,
                message = "Vungle SDK is not initialized yet."
            )
            adStateManager.onAdFailedToLoad(adError)
            adLoadListener?.onAdFailedToLoad(adError)
            return
        }
        if (!adConfig.isTestModeEnabled) {
            checkNotNull(placementId) { "placementId must be set." }
            require(placementId.isNotEmpty()) { "placementId must not be empty." }
        }
        val finalPlacementId = if (adConfig.isTestModeEnabled) TEST_AD_UNIT_ID else placementId

        mRewarded = RewardedAd(context, finalPlacementId, com.vungle.ads.AdConfig())
        mRewarded!!.adListener = rewardedAdListener
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
            mRewarded!!.play(owner)
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
        fun with(context: Context, placementId: String): VungleRewardedAd {
            return VungleRewardedAd(context, placementId, null, null)
        }

        @JvmStatic
        fun with(context: Context, @StringRes placementIdRes: Int): VungleRewardedAd {
            return with(context, context.getString(placementIdRes))
        }

        const val TEST_AD_UNIT_ID: String = "R1-9273153"
        const val TAG: String = "VungleRewardedAd"
    }
}
