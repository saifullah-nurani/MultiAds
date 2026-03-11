package io.github.saifullah.nurani.ads.core

/**
 * Defines when an ad should be reloaded after an event occurs.
 */
enum class AdReloadPolicy {
    /**
     * Reload the ad after the user clicks it.
     * Useful when you want a fresh ad ready after interaction.
     */
    OnClicked,

    /**
     * Reload the ad after it is dismissed/closed by the user.
     */
    OnDismissed,

    /**
     * Reload the ad if it fails to show.
     * Helps recover automatically from show errors.
     */
    OnFailedToShow
}