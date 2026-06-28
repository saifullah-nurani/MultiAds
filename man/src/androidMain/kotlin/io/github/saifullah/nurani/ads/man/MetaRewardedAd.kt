package io.github.saifullah.nurani.ads.man

import android.app.Activity
import android.content.Context
import android.os.Handler
import androidx.annotation.StringRes
import androidx.lifecycle.LifecycleOwner
import com.facebook.ads.Ad
import com.facebook.ads.AdError
import com.facebook.ads.RewardedVideoAd
import com.facebook.ads.RewardedVideoAdListener
import io.github.saifullah.nurani.ads.core.AdConfig
import io.github.saifullah.nurani.ads.core.AdError as CoreAdError
import io.github.saifullah.nurani.ads.core.OnUserRewardedListener
import io.github.saifullah.nurani.ads.core.Scheduler
import io.github.saifullah.nurani.ads.core.compose.PlatformActivity
import io.github.saifullah.nurani.ads.core.utils.ContextUtils.Companion.findActivity

class MetaRewardedAd(
    val context: Context,
    val placementId: String,
    adConfig: AdConfig?,
    handler: Handler?
) : MetaRewardedAdState(context, Scheduler(handler), adConfig, TAG) {
    private var mRewarded: RewardedVideoAd? = null

    private val rewardedAdListener = object : RewardedVideoAdListener {
        override fun onError(ad: Ad?, adError: AdError?) {
            val error =
                if (adError != null) MetaUtils.adErrorFrom(adError) else CoreAdError(
                    -1,
                    "Unknown error"
                )
            adStateManager.onAdFailedToLoad(error)
            adLoadListener?.onAdFailedToLoad(error)
            if (adStateManager.shouldPreserveOnFailure) {
                mRewarded = null
            }
        }

        override fun onAdLoaded(ad: Ad?) {
            adStateManager.onAdLoaded()
            adLoadListener?.onAdLoaded()
        }

        override fun onAdClicked(ad: Ad?) {
            adStateManager.onAdClicked()
            adScreenContentCallback?.onAdClicked()
        }

        override fun onLoggingImpression(ad: Ad?) {
            adStateManager.onAdDisplayed()
            adScreenContentCallback?.onAdDisplayed()
        }

        override fun onRewardedVideoCompleted() {
            userRewardedCallback?.invoke()
        }

        override fun onRewardedVideoClosed() {
            clean()
            adStateManager.onAdDismissed()
            adScreenContentCallback?.onAdDismissed()
        }
    }

    override val isAdAvailable: Boolean get() = mRewarded != null && mRewarded!!.isAdLoaded

    override fun loadAd() {
        if (mRewarded != null && mRewarded!!.isAdLoaded) return
        reloadAd()
    }

    override fun onAdLoad() {
        if (!adConfig.isTestModeEnabled) {
            checkNotNull(placementId) { "placementId must be set." }
            require(placementId.isNotEmpty()) { "placementId must not be empty." }
        }
        val finalPlacementId = if (adConfig.isTestModeEnabled) TEST_AD_UNIT_ID else placementId

        mRewarded?.destroy()

        val ad = RewardedVideoAd(context, finalPlacementId)
        mRewarded = ad
        val config = ad.buildLoadAdConfig()
            .withAdListener(rewardedAdListener)
            .build()
        ad.loadAd(config)
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
        fun with(context: Context, placementId: String): MetaRewardedAd {
            return MetaRewardedAd(context, placementId, null, null)
        }

        @JvmStatic
        fun with(context: Context, @StringRes placementIdRes: Int): MetaRewardedAd {
            return with(context, context.getString(placementIdRes))
        }

        const val TEST_AD_UNIT_ID: String = "VID_HD_9_16_39S_APP_INSTALL#YOUR_PLACEMENT_ID"

        const val TAG: String = "MetaRewardedAd"
    }
}
