package io.github.saifullah.nurani.ads.vungle

import android.app.Activity
import android.content.Context
import android.os.Handler
import androidx.annotation.StringRes
import androidx.lifecycle.LifecycleOwner
import com.vungle.ads.BaseAd
import com.vungle.ads.InterstitialAd
import com.vungle.ads.InterstitialAdListener
import com.vungle.ads.VungleError
import io.github.saifullah.nurani.ads.core.AdConfig
import io.github.saifullah.nurani.ads.core.Scheduler
import io.github.saifullah.nurani.ads.core.utils.ContextUtils.Companion.findActivity

class VungleInterstitialAd(
    val context: Context,
    val placementId: String,
    adConfig: AdConfig?,
    handler: Handler?
) : FullScreenAdState(context, Scheduler(handler), adConfig, TAG) {
    private var mInterstitialAd: InterstitialAd? = null

    private val interstitialAdListener = object : InterstitialAdListener {
        override fun onAdLoaded(baseAd: BaseAd) {
            adStateManager.onAdLoaded()
            adLoadListener?.onAdLoaded()
        }

        override fun onAdFailedToLoad(baseAd: BaseAd, adError: VungleError) {
            val adError = VungleUtils.adErrorFrom(adError)
            adStateManager.onAdFailedToLoad(adError)
            adLoadListener?.onAdFailedToLoad(adError)
            if (adStateManager.shouldPreserveOnFailure) {
                mInterstitialAd = null
            }
        }

        override fun onAdStart(baseAd: BaseAd) {
            adStateManager.onAdShowed()
            adScreenContentCallback?.onAdShowed()
        }

        override fun onAdClicked(baseAd: BaseAd) {
            adStateManager.onAdClicked()
            adScreenContentCallback?.onAdClicked()
        }

        override fun onAdEnd(baseAd: BaseAd) {
            clean()
            adStateManager.onAdDismissed()
            adScreenContentCallback?.onAdDismissed()
        }

        override fun onAdFailedToPlay(baseAd: BaseAd, adError: VungleError) {
            val adError = VungleUtils.adErrorFrom(adError)
            adScreenContentCallback?.onAdFailedToShow(adError)
            clean()
        }

        override fun onAdLeftApplication(baseAd: BaseAd) {}
        override fun onAdImpression(baseAd: BaseAd) {}
    }

    override val isAdAvailable: Boolean get() = mInterstitialAd != null && mInterstitialAd!!.canPlayAd()

    override fun loadAd() {
        if (mInterstitialAd != null && mInterstitialAd!!.canPlayAd()) return
        reloadAd()
    }

    override fun onAdLoad() {
        if (!adConfig.isTestModeEnabled) {
            checkNotNull(placementId) { "placementId must be set." }
            require(placementId.isNotEmpty()) { "placementId must not be empty." }
        }
        val finalPlacementId = if (adConfig.isTestModeEnabled) TEST_AD_UNIT_ID else placementId

        mInterstitialAd = InterstitialAd(context, finalPlacementId, com.vungle.ads.AdConfig())
        mInterstitialAd!!.adListener = interstitialAdListener
        mInterstitialAd!!.load()
    }

    override fun clean() {
        mInterstitialAd = null
    }

    override fun showAd(owner: Activity) {
        if (isAdAvailable) {
            mInterstitialAd!!.play(owner)
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
        fun with(context: Context, placementId: String): VungleInterstitialAd {
            return VungleInterstitialAd(context, placementId, null, null)
        }

        @JvmStatic
        fun with(context: Context, @StringRes placementIdRes: Int): VungleInterstitialAd {
            return with(context, context.getString(placementIdRes))
        }

        const val TEST_AD_UNIT_ID: String = "INTERSTITIAL-1491904"
        const val TAG: String = "VungleInterstitialAd"
    }
}
