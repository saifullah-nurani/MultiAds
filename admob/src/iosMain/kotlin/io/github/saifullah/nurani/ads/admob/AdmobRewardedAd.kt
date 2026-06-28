package io.github.saifullah.nurani.ads.admob

import GoogleMobileAds.GADRequest
import GoogleMobileAds.GADRewardedAd
import io.github.saifullah.nurani.ads.admob.utils.adErrorFrom
import io.github.saifullah.nurani.ads.core.AdConfig
import io.github.saifullah.nurani.ads.core.Scheduler
import io.github.saifullah.nurani.ads.core.compose.PlatformActivity
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSError
import platform.UIKit.UIViewController

@OptIn(ExperimentalForeignApi::class)
class AdmobRewardedAd(
    adUnitId: String,
    uIViewController: UIViewController?,
    adConfig: AdConfig?,
    adRequest: GADRequest?,
) : RewardedAdState(uIViewController, Scheduler(), adConfig, TAG) {
    private var mRewardedAd: GADRewardedAd? = null
    private var adDelegate: GoogleMobileAds.GADFullScreenContentDelegateProtocol? = null
    private val adUnitId: String =
        if (this.adConfig.isTestModeEnabled) TEST_AD_UNIT_ID else adUnitId
    private val adRequest = adRequest ?: GADRequest()

    override val isAdAvailable: Boolean get() = mRewardedAd != null

    override fun loadAd() {
        if (mRewardedAd != null) return
        reloadAd()
    }


    override fun onAdLoad() {
        GADRewardedAd.loadWithAdUnitID(
            adUnitId,
            adRequest
        ) { ad: GADRewardedAd?, error: NSError? ->
            if (error != null) {
                adStateManager.onAdFailedToLoad(error.adErrorFrom())
                adLoadListener?.onAdFailedToLoad(error.adErrorFrom())
                if (adStateManager.shouldPreserveOnFailure) {
                    mRewardedAd = null
                }
                return@loadWithAdUnitID
            }

            mRewardedAd = ad
            adStateManager.onAdLoaded()
            adLoadListener?.onAdLoaded()
        }
    }

    override fun clean() {
        mRewardedAd = null
        adDelegate = null
    }

    override fun showAd(owner: UIViewController) {
        showAd(owner, onUserRewarded = {})
    }


    private fun showAd(controller: UIViewController?, onUserRewarded: () -> Unit = {}) {
        if (isAdAvailable) {
            println("AdmobRewardedAd [iOS]: showAd called, userRewardedCallback is $userRewardedCallback")
            val delegate = fullScreenContentCallback(adStateManager, adScreenContentCallback, ::clean)
            adDelegate = delegate
            mRewardedAd!!.fullScreenContentDelegate = delegate
            mRewardedAd!!.presentFromRootViewController(controller) {
                println("AdmobRewardedAd [iOS]: presentFromRootViewController reward handler triggered")
                onUserRewarded()
                userRewardedCallback?.invoke()
            }
        }
    }

    fun showAd(owner: PlatformActivity?) {
        showAd(controller = owner)
    }

    override fun showAd(onUserRewarded: () -> Unit) {
        showAd(controller = null, onUserRewarded = {})
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

        fun with(adUnitId: String): AdmobRewardedAd {
            return AdmobRewardedAd(adUnitId, null, null, null)
        }

        fun with(adUnitId: String, adConfig: AdConfig): AdmobRewardedAd {
            return AdmobRewardedAd(adUnitId, null, adConfig, null)
        }

        const val TAG: String = "AdmobRewardedAd"
        const val TEST_AD_UNIT_ID: String = "ca-app-pub-3940256099942544/1712485313"
    }
}
