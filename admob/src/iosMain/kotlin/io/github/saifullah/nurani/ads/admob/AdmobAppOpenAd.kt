package io.github.saifullah.nurani.ads.admob

import GoogleMobileAds.GADAppOpenAd
import GoogleMobileAds.GADRequest
import io.github.saifullah.nurani.ads.admob.utils.adErrorFrom
import io.github.saifullah.nurani.ads.core.AdConfig
import io.github.saifullah.nurani.ads.core.AppOpenAd
import io.github.saifullah.nurani.ads.core.Scheduler
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSError
import platform.UIKit.UIViewController

@OptIn(ExperimentalForeignApi::class)
class AdmobAppOpenAd(
    adUnitId: String,
    uIViewController: UIViewController?,
    adConfig: AdConfig?,
    adRequest: GADRequest?,
) : FullScreenAdState(uIViewController, Scheduler(), adConfig, TAG), AppOpenAd {

    private var mAppOpenAd: GADAppOpenAd? = null
    private val adUnitId: String = if (this.adConfig.isTestModeEnabled) TEST_AD_UNIT_ID else adUnitId
    private val adRequest: GADRequest = adRequest ?: GADRequest()

    override val isAdAvailable: Boolean get() = mAppOpenAd != null

    override fun loadAd() {
        if (mAppOpenAd != null) return
        reloadAd()
    }

    override fun onAdLoad() {
        GADAppOpenAd.loadWithAdUnitID(
            adUnitId,
            adRequest
        ) { ad: GADAppOpenAd?, error: NSError? ->
            if (error != null) {
                adStateManager.onAdFailedToLoad(error.adErrorFrom())
                adLoadListener?.onAdFailedToLoad(error.adErrorFrom())
                if (adStateManager.shouldPreserveOnFailure) {
                    mAppOpenAd = null
                }
                return@loadWithAdUnitID
            }

            mAppOpenAd = ad
            adStateManager.onAdLoaded()
            adLoadListener?.onAdLoaded()
        }
    }

    override fun clean() {
        mAppOpenAd = null
    }

    private fun showAdNow(controller: UIViewController?) {
        if (isAdAvailable) {
            mAppOpenAd!!.fullScreenContentDelegate = fullScreenContentCallback(adStateManager, adScreenContentCallback, ::clean)
            mAppOpenAd!!.presentFromRootViewController(controller)
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
        fun with(adUnitId: String): AdmobAppOpenAd {
            return AdmobAppOpenAd(adUnitId, null, null, null)
        }

        fun with(adUnitId: String, adConfig: AdConfig): AdmobAppOpenAd {
            return AdmobAppOpenAd(adUnitId, null, adConfig, null)
        }

        const val TAG: String = "AdmobAppOpenAd"
        const val TEST_AD_UNIT_ID: String = "ca-app-pub-3940256099942544/5575461041"
    }
}
