package io.github.saifullah.nurani.ads.multi.models

import io.github.saifullah.nurani.ads.core.AdError

interface MultiAdListener {
    fun onAdLoaded(network: AdNetworkConfig) {}
    fun onAdFailedToLoad(network: AdNetworkConfig, error: AdError?) {}
    fun onAdFailedToShow(network: AdNetworkConfig, error: AdError?) {}
    fun onAdShowed(network: AdNetworkConfig) {}
    fun onAdDisplayed(network: AdNetworkConfig) {}
    fun onAdDismissed(network: AdNetworkConfig) {}
    fun onAdClicked(network: AdNetworkConfig) {}
}
