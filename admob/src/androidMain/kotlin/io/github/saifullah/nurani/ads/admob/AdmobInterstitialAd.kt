package io.github.saifullah.nurani.ads.admob

import android.app.Activity
import android.content.Context
import android.os.Handler
import androidx.annotation.StringRes
import androidx.lifecycle.LifecycleOwner
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import io.github.saifullah.nurani.ads.core.AdConfig
import io.github.saifullah.nurani.ads.core.Scheduler
import io.github.saifullah.nurani.ads.core.utils.ContextUtils.Companion.findActivity

class AdmobInterstitialAd(
    val context: Context,
    adUnitId: String,
    adConfig: AdConfig?,
    adRequest: AdRequest?,
    handler: Handler?
) : FullScreenAdState(context, Scheduler(handler), adConfig, TAG) {
    private var mInterstitialAd: InterstitialAd? = null
    private val adUnitId: String = if (this.adConfig.isTestModeEnabled) TEST_AD_UNIT_ID else adUnitId
    private val adRequest: AdRequest = adRequest ?: AdRequest.Builder().build()

    private val interstitialAdLoadCallback: InterstitialAdLoadCallback =
        object : InterstitialAdLoadCallback() {
            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                adStateManager.onAdLoaded()
                adLoadListener?.onAdLoaded()
                mInterstitialAd = interstitialAd
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                val adError = AdmobUtils.adErrorFrom(loadAdError)
                adStateManager.onAdFailedToLoad(adError)
                adLoadListener?.onAdFailedToLoad(adError)
                if (adStateManager.shouldPreserveOnFailure) {
                    mInterstitialAd = null
                }
            }
        }


    override val isAdAvailable: Boolean get() = mInterstitialAd != null

    override fun loadAd() {
        if (mInterstitialAd != null) return
        reloadAd()
    }


    override fun onAdLoad() {
        InterstitialAd.load(context, adUnitId, adRequest, interstitialAdLoadCallback)
    }

    override fun clean() {
        mInterstitialAd = null
    }

    override fun showAd(owner: Activity) {
        if (isAdAvailable) {
            mInterstitialAd!!.setImmersiveMode(isImmersiveModeEnabled)
            mInterstitialAd!!.fullScreenContentCallback = FullScreenContentCallback(adStateManager, adScreenContentCallback, ::clean)
            mInterstitialAd!!.show(owner)
        }
    }

    @Throws(IllegalStateException::class)
    fun showAdOrThrow() {
        val activity = findActivity(context)
        checkNotNull(activity) { "${TAG}: Context is not an Activity." }

        check(isAdAvailable) { "${TAG}: is not loaded." }
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
        fun with(context: Context, adUnitId: String): AdmobInterstitialAd {
            return AdmobInterstitialAd(context, adUnitId, null, null, null)
        }

        @JvmStatic
        fun with(context: Context, @StringRes adUnitId: Int): AdmobInterstitialAd {
            return with(context, context.getString(adUnitId))
        }

        const val TAG: String = "AdmobInterstitialAd"
        const val TEST_AD_UNIT_ID: String = "ca-app-pub-3940256099942544/1033173712"
    }
}
