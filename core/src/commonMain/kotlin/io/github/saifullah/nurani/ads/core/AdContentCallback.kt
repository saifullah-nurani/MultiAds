package io.github.saifullah.nurani.ads.core
import androidx.compose.runtime.Stable
/**
 * Creates an instance of [AdContentCallback] using lambda expressions.
 *
 * This helper function allows consumers to implement only the callbacks
 * they care about without creating a full anonymous class manually.
 *
 * Example usage:
 *
 * AdContentCallback(
 *     onAdDismissed = { println("Ad dismissed") },
 *     onAdClicked = { println("Ad clicked") }
 * )
 *
 * All parameters have default empty implementations,
 * so any callback can be safely omitted.
 *
 * @param onAdFailedToShow Called when the ad fails to show.
 * @param onAdShowed Called when the ad is shown to the user.
 * @param onAdDisplayed Called when the ad is displayed (if applicable).
 * @param onAdDismissed Called when the ad is dismissed by the user.
 * @param onAdClicked Called when the ad is clicked.
 *
 * @return An implementation of [AdContentCallback].
 */
inline fun AdContentCallback(
    crossinline onAdFailedToShow: (AdError?) -> Unit = {},
    crossinline onAdShowed: () -> Unit = {},
    crossinline onAdDisplayed: () -> Unit = {},
    crossinline onAdDismissed: () -> Unit = {},
    crossinline onAdClicked: () -> Unit = {}
): AdContentCallback = object : AdContentCallback {
    override fun onAdFailedToShow(error: AdError?) = onAdFailedToShow(error)
    override fun onAdShowed() = onAdShowed()
    override fun onAdDisplayed() = onAdDisplayed()
    override fun onAdDismissed() = onAdDismissed()
    override fun onAdClicked() = onAdClicked()
}

@Stable
interface AdContentCallback {
    /**
     * Called when the ad failed to show.
     *
     * @param error Provides details about why the ad failed to display.
     */
    fun onAdFailedToShow(error: AdError?)

    /**
     * Called when the ad show process has started.
     */
    fun onAdShowed()

    /**
     * Called when the ad is fully displayed and visible to the user.
     */
    fun onAdDisplayed()

    /**
     * Called when the ad is dismissed or closed by the user or system.
     */
    fun onAdDismissed()

    /**
     * Called when the user clicks the ad.
     */
    fun onAdClicked()
}