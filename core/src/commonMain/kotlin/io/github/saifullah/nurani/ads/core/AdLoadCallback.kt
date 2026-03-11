package io.github.saifullah.nurani.ads.core
import androidx.compose.runtime.Stable

/**
 * Creates an instance of [AdLoadCallback] using lambda expressions.
 *
 * This helper function simplifies the implementation of load callbacks
 * by allowing consumers to provide only the required lambdas.
 *
 * Example usage:
 *
 * AdLoadCallback(
 *     onAdLoaded = {
 *         println("Ad loaded successfully")
 *     },
 *     onAdLoadFailed = { error ->
 *         println("Ad failed: $error")
 *     }
 * )
 *
 * All parameters have default empty implementations,
 * so either callback can be safely omitted.
 *
 * @param onAdFailedToLoad Called when the ad fails to load.
 * @param onAdLoaded Called when the ad successfully loads.
 *
 * @return An implementation of [AdLoadCallback].
 */
inline fun AdLoadCallback(
    crossinline onAdFailedToLoad: (AdError?) -> Unit = {},
    crossinline onAdLoaded: () -> Unit = {}
): AdLoadCallback = object : AdLoadCallback {
    override fun onAdFailedToLoad(error: AdError?) = onAdFailedToLoad(error)
    override fun onAdLoaded() = onAdLoaded()
}

@Stable
interface AdLoadCallback {
    /**
     * Called when the interstitial ad failed to load.
     *
     * @param error The [AdError] describing the reason for failure.
     * This may include error code, message, and optional cause.
     */
    fun onAdFailedToLoad(error: AdError?)

    /**
     * Called when the interstitial ad is successfully loaded and ready to show.
     *
     *
     * After this callback, you can safely call the show method of the ad.
     */
    fun onAdLoaded()
}