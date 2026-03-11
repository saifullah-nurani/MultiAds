package io.github.saifullah.nurani.ads.admob

import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.FullScreenContentCallback
import io.github.saifullah.nurani.ads.core.AdContentCallback
import io.github.saifullah.nurani.ads.core.AdStateManager

fun FullScreenContentCallback(
    adStateManager: AdStateManager,
    adContentCallback: AdContentCallback?,
    cleanUp: () -> Unit
) = object : FullScreenContentCallback() {
    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
        cleanUp()
        val error = AdmobUtils.adErrorFrom(adError)
        adStateManager.onAdFailedToShow(error)
        adContentCallback?.onAdFailedToShow(error)
    }

    override fun onAdShowedFullScreenContent() {
        adStateManager.onAdShowed()
        adContentCallback?.onAdShowed()
    }

    override fun onAdDismissedFullScreenContent() {
        cleanUp()
        adStateManager.onAdDismissed()
        adContentCallback?.onAdDismissed()
    }

    override fun onAdImpression() {
        adStateManager.onAdDisplayed()
        adContentCallback?.onAdDisplayed()
    }

    override fun onAdClicked() {
        adStateManager.onAdClicked()
        adContentCallback?.onAdClicked()
    }
}