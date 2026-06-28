package io.github.saifullah.nurani.ads.multi.models

import io.github.saifullah.nurani.ads.core.AdError

interface MultiAdLoadCallback {
    fun onAdLoaded(network: AdNetworkConfig) {}
    fun onAdFailedToLoad(network: AdNetworkConfig, error: AdError?) {}
}

/**
 * Creates an instance of [MultiAdLoadCallback] using lambda expressions.
 * This helper function allows consumers to implement only the callbacks they care about.
 *
 * Example:
 * ```
 * MultiAdLoadCallback(
 *     onAdLoaded = { network -> println("Loaded from ${network.network}") }
 * )
 * ```
 */
inline fun MultiAdLoadCallback(
    crossinline onAdLoaded: (network: AdNetworkConfig) -> Unit = {},
    crossinline onAdFailedToLoad: (network: AdNetworkConfig, error: AdError?) -> Unit = { _, _ -> }
): MultiAdLoadCallback = object : MultiAdLoadCallback {
    override fun onAdLoaded(network: AdNetworkConfig) = onAdLoaded(network)
    override fun onAdFailedToLoad(network: AdNetworkConfig, error: AdError?) = onAdFailedToLoad(network, error)
}
