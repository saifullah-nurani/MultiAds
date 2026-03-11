package io.github.saifullah.nurani.ads.core

interface OnUserRewardedListener {
    /**
     * Called when the user has earned the reward from a rewarded ad.
     *
     *
     * This indicates the reward condition defined by the ad network
     * has been satisfied (e.g., full video watched).
     */
    fun onUserRewarded()
}