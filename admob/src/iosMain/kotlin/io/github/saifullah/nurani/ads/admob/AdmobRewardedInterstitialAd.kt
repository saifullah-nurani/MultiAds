package io.github.saifullah.nurani.ads.admob

import GoogleMobileAds.GADRequest
import GoogleMobileAds.GADRewardedInterstitialAd
import io.github.saifullah.nurani.ads.admob.utils.adErrorFrom
import io.github.saifullah.nurani.ads.core.AdConfig
import io.github.saifullah.nurani.ads.core.Scheduler
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSError
import platform.UIKit.UIViewController

@OptIn(ExperimentalForeignApi::class)
class AdmobRewardedInterstitialAd(
    adUnitId: String,
    uIViewController: UIViewController?,
    adConfig: AdConfig?,
    adRequest: GADRequest?,
) : RewardedAdState(activity = uIViewController, Scheduler(), adConfig, TAG) {
    private var mRewardedInterstitialAd: GADRewardedInterstitialAd? = null
    private val adUnitId: String =
        if (this.adConfig.isTestModeEnabled) TEST_AD_UNIT_ID else adUnitId
    private val adRequest = adRequest ?: GADRequest()

    override val isAdAvailable: Boolean get() = mRewardedInterstitialAd != null

    override fun loadAd() {
        if (mRewardedInterstitialAd != null) return
        reloadAd()
    }


    override fun onAdLoad() {
        GADRewardedInterstitialAd.loadWithAdUnitID(
            adUnitId,
            adRequest
        ) { ad: GADRewardedInterstitialAd?, error: NSError? ->
            if (error != null) {
                adStateManager.onAdFailedToLoad(error.adErrorFrom())
                adLoadListener?.onAdFailedToLoad(error.adErrorFrom())
                if (adStateManager.shouldPreserveOnFailure) {
                    mRewardedInterstitialAd = null
                }
                return@loadWithAdUnitID
            }

            mRewardedInterstitialAd = ad
            adStateManager.onAdLoaded()
            adLoadListener?.onAdLoaded()
        }
    }

    override fun clean() {
        mRewardedInterstitialAd = null
    }

    override fun showAd(owner: UIViewController) {
        showAd(owner, onUserRewarded = {})
    }

    fun showAd(owner: UIViewController?) {
        showAd(controller = owner)
    }

    private fun showAd(controller: UIViewController?, onUserRewarded: () -> Unit = {}) {
        if (isAdAvailable) {
            mRewardedInterstitialAd!!.fullScreenContentDelegate =
                fullScreenContentCallback(adStateManager, adScreenContentCallback, ::clean)
            mRewardedInterstitialAd!!.presentFromRootViewController(controller) {
                onUserRewarded()
                userRewardedCallback?.invoke()
            }
        }
    }

    override fun showAd(onUserRewarded: () -> Unit) {
        showAd(null, onUserRewarded)
    }

    override fun showAd(
        owner: UIViewController,
        onUserRewarded: () -> Unit
    ) {
        showAd(controller = owner, onUserRewarded)
    }

    override fun tryShowAd(): Boolean {
        showAd(null)
        return true
    }

    companion object {

        fun with(adUnitId: String): AdmobRewardedInterstitialAd {
            return AdmobRewardedInterstitialAd(adUnitId, null, null, null)
        }

        fun with(adUnitId: String, adConfig: AdConfig): AdmobRewardedInterstitialAd {
            return AdmobRewardedInterstitialAd(adUnitId, null, adConfig, null)
        }

        const val TAG: String = "AdmobRewardedInterstitialAd"
        const val TEST_AD_UNIT_ID: String = "ca-app-pub-3940256099942544/6978759866"
    }
}
