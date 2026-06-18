package io.github.saifullah.nurani.ads.man

import android.app.Activity
import android.content.Context
import android.os.Handler
import androidx.annotation.StringRes
import androidx.lifecycle.LifecycleOwner
import com.facebook.ads.Ad
import com.facebook.ads.AdError
import com.facebook.ads.InterstitialAd
import com.facebook.ads.InterstitialAdListener
import io.github.saifullah.nurani.ads.core.AdConfig
import io.github.saifullah.nurani.ads.core.Scheduler
import io.github.saifullah.nurani.ads.core.utils.ContextUtils.Companion.findActivity

class MetaInterstitialAd(
    val context: Context,
    val placementId: String,
    adConfig: AdConfig?,
    handler: Handler?
) : MetaFullScreenAdState(context, Scheduler(handler), adConfig, TAG) {
    private var mInterstitialAd: InterstitialAd? = null

    private val interstitialAdListener = object : InterstitialAdListener {
        override fun onInterstitialDisplayed(ad: Ad?) {
            adStateManager.onAdShowed()
            adScreenContentCallback?.onAdShowed()
        }

        override fun onInterstitialDismissed(ad: Ad?) {
            clean()
            adStateManager.onAdDismissed()
            adScreenContentCallback?.onAdDismissed()
        }

        override fun onError(ad: Ad?, adError: AdError?) {
            val error =
                if (adError != null) MetaUtils.adErrorFrom(adError) else io.github.saifullah.nurani.ads.core.AdError(
                    -1,
                    "Unknown error"
                )
            adStateManager.onAdFailedToLoad(error)
            adLoadListener?.onAdFailedToLoad(error)
            if (adStateManager.shouldPreserveOnFailure) {
                mInterstitialAd = null
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
    }

    override val isAdAvailable: Boolean get() = mInterstitialAd != null && mInterstitialAd!!.isAdLoaded

    override fun loadAd() {
        if (mInterstitialAd != null && mInterstitialAd!!.isAdLoaded) return
        reloadAd()
    }

    override fun onAdLoad() {
        if (!adConfig.isTestModeEnabled) {
            checkNotNull(placementId) { "placementId must be set." }
            require(placementId.isNotEmpty()) { "placementId must not be empty." }
        }
        val finalPlacementId = if (adConfig.isTestModeEnabled) TEST_AD_UNIT_ID else placementId

        mInterstitialAd?.destroy()

        val ad = InterstitialAd(context, finalPlacementId)
        mInterstitialAd = ad
        val config = ad.buildLoadAdConfig()
            .withAdListener(interstitialAdListener)
            .build()
        ad.loadAd(config)
    }

    override fun clean() {
        mInterstitialAd?.destroy()
        mInterstitialAd = null
    }

    override fun showAd(owner: Activity) {
        if (isAdAvailable) {
            mInterstitialAd!!.show()
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
        fun with(context: Context, placementId: String): MetaInterstitialAd {
            return MetaInterstitialAd(context, placementId, null, null)
        }

        @JvmStatic
        fun with(context: Context, @StringRes placementIdRes: Int): MetaInterstitialAd {
            return with(context, context.getString(placementIdRes))
        }

        const val TEST_AD_UNIT_ID: String = "IMG_16_9_APP_INSTALL#YOUR_PLACEMENT_ID"

        const val TAG: String = "MetaInterstitialAd"
    }
}
