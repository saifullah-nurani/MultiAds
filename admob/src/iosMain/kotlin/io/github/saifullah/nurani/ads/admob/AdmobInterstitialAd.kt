package io.github.saifullah.nurani.ads.admob

import GoogleMobileAds.GADInterstitialAd
import GoogleMobileAds.GADRequest
import io.github.saifullah.nurani.ads.admob.utils.adErrorFrom
import io.github.saifullah.nurani.ads.core.AdConfig
import io.github.saifullah.nurani.ads.core.Scheduler
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSError
import platform.UIKit.UIViewController

@OptIn(ExperimentalForeignApi::class)
class AdmobInterstitialAd(
    adUnitId: String,
    uIViewController: UIViewController?,
    adConfig: AdConfig?,
    adRequest: GADRequest?,
) : FullScreenAdState(uIViewController, Scheduler(), adConfig, TAG) {

    private var mInterstitialAd: GADInterstitialAd? = null
    private val adUnitId: String = if (this.adConfig.isTestModeEnabled) TEST_AD_UNIT_ID else adUnitId
    private val adRequest: GADRequest = adRequest ?: GADRequest()

    override val isAdAvailable: Boolean get() = mInterstitialAd != null

    override fun loadAd() {
        if (mInterstitialAd != null) return
        reloadAd()
    }


    override fun onAdLoad() {
        GADInterstitialAd.loadWithAdUnitID(
            adUnitId,
            adRequest
        ) { ad: GADInterstitialAd?, error: NSError? ->
            if (error != null) {
                adStateManager.onAdFailedToLoad(error.adErrorFrom())
                adLoadListener?.onAdFailedToLoad(error.adErrorFrom())
                if (adStateManager.shouldPreserveOnFailure) {
                    mInterstitialAd = null
                }
                return@loadWithAdUnitID
            }

            mInterstitialAd = ad
            adStateManager.onAdLoaded()
            adLoadListener?.onAdLoaded()
        }

    }

    override fun clean() {
        mInterstitialAd = null
    }

    private fun showAdNow(controller: UIViewController?) {
        if (isAdAvailable) {
            mInterstitialAd!!.fullScreenContentDelegate = fullScreenContentCallback(adStateManager, adScreenContentCallback, ::clean)
            mInterstitialAd!!.presentFromRootViewController(controller)
        }
    }

    override fun showAd(owner: UIViewController) {
        showAdNow(owner)
    }

    override fun tryShowAd(): Boolean {
        showAdNow(null)
        return true
    }

    fun showAd(owner: UIViewController?) {
        showAdNow(owner)
    }

    companion object {

        fun with(adUnitId: String): AdmobInterstitialAd {
            return AdmobInterstitialAd(adUnitId, null, null, null)
        }

        fun with(adUnitId: String, adConfig: AdConfig): AdmobInterstitialAd {
            return AdmobInterstitialAd(adUnitId, null, adConfig, null)
        }

        const val TAG: String = "AdmobInterstitialAd"
        const val TEST_AD_UNIT_ID: String = "ca-app-pub-3940256099942544/4411468910"
    }
}
