package io.github.saifullah.nurani.ads.core

import androidx.compose.runtime.Stable

/**
 * Represents a generic Ad unit abstraction.
 *
 *
 * This interface defines the basic contract that every ad implementation
 * (Interstitial, Rewarded, Banner, etc.) must follow.
 *
 *
 * It allows:
 *
 *  * Checking ad availability and loading state
 *  * Loading and reloading ads
 *  * Receiving lifecycle callbacks for load and content events
 *
 *
 *
 * Implementations are responsible for handling internal state management,
 * retry logic, refresh behavior, and network-specific SDK integration.
 */
@Stable
interface AdState {
    /**
     * Returns whether an ad is currently loaded and ready to be shown.
     *
     * @return true if ad is available, false otherwise.
     */
    val isAdAvailable: Boolean

    /**
     * Returns whether an ad is currently being loaded.
     *
     *
     * This can be used to prevent duplicate load requests.
     *
     * @return true if loading is in progress, false otherwise.
     */
    val isAdLoading: Boolean

    /**
     * Indicates whether the ad is currently retrying after a failed load attempt.
     */
    val isRetryingAdFailedLoad: Boolean

    /**
     * Indicates whether the ad is refreshing  automatically.
     */
    val isAdRefreshing: Boolean

    /**
     * Indicates whether the ad is reloading automatically.
     */
    val isAdReloading: Boolean

    /**
     * Number of ad load attempts made so far.
     */
    val attemptCount: Int

    /**
     * Starts loading an ad if not already loaded.
     *
     *
     * If an ad is already available, this method may ignore the request.
     */
    fun loadAd()

    /**
     * Forces an ad reload attempt.
     *
     *
     * This method should respect internal retry rules,
     * reload policies, and refresh strategies.
     */
    fun reloadAd()

    /**
     * Sets a callback to listen for ad load events.
     *
     *
     * Includes:
     *
     *  * Ad successfully loaded
     *  * Ad failed to load
     *
     *
     * @param callback The load callback listener, or null to remove.
     */
    fun setAdLoadCallback(callback: AdLoadCallback?)

    /**
     * Sets a callback to listen for ad content lifecycle events.
     *
     *
     * Includes:
     *
     *  * Ad shown
     *  * Ad dismissed
     *  * Ad clicked
     *  * Ad impression recorded
     *  * Ad failed to show
     *
     *
     * @param callback The content callback listener, or null to remove.
     */
    fun setAdContentCallback(callback: AdContentCallback?)

}