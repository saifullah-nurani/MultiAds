package io.github.saifullah.nurani.ads.multi.models

import io.github.saifullah.nurani.ads.core.AdError

interface MultiAdContentCallback {
    fun onAdShowed(network: AdNetworkConfig) {}
    fun onAdDisplayed(network: AdNetworkConfig) {}
    fun onAdDismissed(network: AdNetworkConfig) {}
    fun onAdClicked(network: AdNetworkConfig) {}
    fun onAdFailedToShow(network: AdNetworkConfig, error: AdError?) {}
}

/**
 * Creates an instance of [MultiAdContentCallback] using lambda expressions.
 * This helper function allows consumers to implement only the callbacks they care about.
 *
 * Example:
 * ```
 * MultiAdContentCallback(
 *     onAdDismissed = { network -> println("Dismissed from ${network.network}") }
 * )
 * ```
 */
inline fun MultiAdContentCallback(
    crossinline onAdShowed: (network: AdNetworkConfig) -> Unit = {},
    crossinline onAdDisplayed: (network: AdNetworkConfig) -> Unit = {},
    crossinline onAdDismissed: (network: AdNetworkConfig) -> Unit = {},
    crossinline onAdClicked: (network: AdNetworkConfig) -> Unit = {},
    crossinline onAdFailedToShow: (network: AdNetworkConfig, error: AdError?) -> Unit = { _, _ -> }
): MultiAdContentCallback = object : MultiAdContentCallback {
    override fun onAdShowed(network: AdNetworkConfig) = onAdShowed(network)
    override fun onAdDisplayed(network: AdNetworkConfig) = onAdDisplayed(network)
    override fun onAdDismissed(network: AdNetworkConfig) = onAdDismissed(network)
    override fun onAdClicked(network: AdNetworkConfig) = onAdClicked(network)
    override fun onAdFailedToShow(network: AdNetworkConfig, error: AdError?) = onAdFailedToShow(network, error)
}
