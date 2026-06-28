package io.github.saifullah.nurani.ads.admob

import GoogleMobileAds.GADFullScreenContentDelegateProtocol
import GoogleMobileAds.GADFullScreenPresentingAdProtocol
import io.github.saifullah.nurani.ads.admob.utils.adErrorFrom
import io.github.saifullah.nurani.ads.core.AdContentCallback
import io.github.saifullah.nurani.ads.core.AdStateManager
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSError
import platform.darwin.NSObject

@OptIn(ExperimentalForeignApi::class)
fun fullScreenContentCallback(
    adStateManager: AdStateManager,
    adContentCallback: AdContentCallback?,
    cleanUp: () -> Unit
): GADFullScreenContentDelegateProtocol =
    object : NSObject(), GADFullScreenContentDelegateProtocol {
        override fun adDidDismissFullScreenContent(ad: GADFullScreenPresentingAdProtocol) {
            println("FullScreenContentCallback [iOS]: adDidDismissFullScreenContent")
            cleanUp()
            adStateManager.onAdDismissed()
            adContentCallback?.onAdDismissed()
        }

        override fun adDidRecordClick(ad: GADFullScreenPresentingAdProtocol) {
            println("FullScreenContentCallback [iOS]: adDidRecordClick")
            adStateManager.onAdClicked()
            adContentCallback?.onAdClicked()
        }

        override fun adDidRecordImpression(ad: GADFullScreenPresentingAdProtocol) {
            println("FullScreenContentCallback [iOS]: adDidRecordImpression")
            adStateManager.onAdDisplayed()
            adContentCallback?.onAdDisplayed()
        }

        override fun ad(
            ad: GADFullScreenPresentingAdProtocol,
            didFailToPresentFullScreenContentWithError: NSError
        ) {
            println("FullScreenContentCallback [iOS]: didFailToPresentFullScreenContentWithError: $didFailToPresentFullScreenContentWithError")
            cleanUp()
            val error = didFailToPresentFullScreenContentWithError.adErrorFrom()
            adStateManager.onAdFailedToShow(error)
            adContentCallback?.onAdFailedToShow(error)
        }

        override fun adWillPresentFullScreenContent(ad: GADFullScreenPresentingAdProtocol) {
            println("FullScreenContentCallback [iOS]: adWillPresentFullScreenContent")
            adStateManager.onAdShowed()
            adContentCallback?.onAdShowed()
        }

        override fun adWillDismissFullScreenContent(ad: GADFullScreenPresentingAdProtocol) {
            println("FullScreenContentCallback [iOS]: adWillDismissFullScreenContent")
        }
    }