package io.github.saifullah.nurani.ads.multi.models

import io.github.saifullah.nurani.ads.core.AdError

interface MultiAdContentCallback {
    fun onAdShowed(network: AdNetworkConfig) {}
    fun onAdDisplayed(network: AdNetworkConfig) {}
    fun onAdDismissed(network: AdNetworkConfig) {}
    fun onAdClicked(network: AdNetworkConfig) {}
    fun onAdFailedToShow(network: AdNetworkConfig, error: AdError?) {}
}
