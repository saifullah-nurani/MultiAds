package io.github.saifullah.nurani.ads.multi.models

import io.github.saifullah.nurani.ads.core.AdError

interface MultiAdLoadCallback {
    fun onAdLoaded(network: AdNetworkConfig) {}
    fun onAdFailedToLoad(network: AdNetworkConfig, error: AdError?) {}
}
