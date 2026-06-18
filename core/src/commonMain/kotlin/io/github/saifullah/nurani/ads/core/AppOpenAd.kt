package io.github.saifullah.nurani.ads.core

import io.github.saifullah.nurani.ads.core.compose.PlatformActivity

/**
 * Interface representing an App Open Ad.
 * Extends [AdState] and provides methods for displaying the ad.
 */
interface AppOpenAd : AdState {
    /**
     * Shows the App Open Ad using the given platform activity.
     */
    fun showAd(activity: PlatformActivity)

    /**
     * Attempts to show the ad if it is loaded.
     * Returns true if the ad was shown successfully, false otherwise.
     */
    fun tryShowAd(): Boolean
}
