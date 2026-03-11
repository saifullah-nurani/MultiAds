package io.github.saifullah.nurani.ads.core
import androidx.compose.runtime.Stable
/**
 * Creates an instance of [BannerAdListener] using lambda expressions.
 *
 * This helper function allows consumers to implement only the callbacks
 * they care about without creating a full anonymous class manually.
 *
 * Example usage:
 *
 * BannerAdListener(
 *     onAdDismissed = { println("Ad dismissed") },
 *     onAdClicked = { println("Ad clicked") }
 * )
 *
 * All parameters have default empty implementations,
 * so any callback can be safely omitted.
 *
 * @param onAdLoaded Called when the ad successfully loads.
 * @param onAdFailedToLoad Called when the ad fails to load.
 * @param onAdFailedToShow Called when the ad fails to show.
 * @param onAdShowed Called when the ad is shown to the user.
 * @param onAdDisplayed Called when the ad is displayed (if applicable).
 * @param onAdDismissed Called when the ad is dismissed by the user.
 * @param onAdClicked Called when the ad is clicked.
 *
 * @return An implementation of [BannerAdListener].
 */
inline fun BannerAdListener(
    crossinline onAdLoaded: () -> Unit = {},
    crossinline onAdFailedToLoad: (AdError?) -> Unit = {},
    crossinline onAdFailedToShow: (AdError?) -> Unit = {},
    crossinline onAdShowed: () -> Unit = {},
    crossinline onAdDisplayed: () -> Unit = {},
    crossinline onAdDismissed: () -> Unit = {},
    crossinline onAdClicked: () -> Unit = {}
): BannerAdListener = object : BannerAdListener {
    override fun onAdFailedToShow(error: AdError?) = onAdFailedToShow(error)
    override fun onAdShowed() = onAdShowed()
    override fun onAdDisplayed() = onAdDisplayed()
    override fun onAdDismissed() = onAdDismissed()
    override fun onAdClicked() = onAdClicked()
    override fun onAdFailedToLoad(error: AdError?) = onAdFailedToLoad(error)
    override fun onAdLoaded() = onAdLoaded()
}

@Stable
interface BannerAdListener : AdLoadCallback, AdContentCallback