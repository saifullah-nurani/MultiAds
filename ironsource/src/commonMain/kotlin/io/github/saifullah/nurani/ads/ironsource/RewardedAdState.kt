package io.github.saifullah.nurani.ads.ironsource

import io.github.saifullah.nurani.ads.core.AdConfig
import io.github.saifullah.nurani.ads.core.Scheduler
import io.github.saifullah.nurani.ads.core.compose.PlatformActivity
import io.github.saifullah.nurani.ads.core.compose.PlatformContext

abstract class RewardedAdState(
    activity: PlatformActivity?,
    scheduler: Scheduler,
    adConfig: AdConfig?,
    tag: String?
) : FullScreenAdState(activity, scheduler, adConfig, tag) {
    protected var userRewardedCallback: (() -> Unit)? = null

    constructor(
        platformContext: PlatformContext,
        scheduler: Scheduler,
        adConfig: AdConfig?,
        tag: String?
    ) : this(null, scheduler, adConfig, tag) {
        setContext(platformContext)
    }

    abstract fun showAd(onUserRewarded: () -> Unit = {})
    abstract fun showAd(owner: PlatformActivity, onUserRewarded: () -> Unit)

    fun setOnUserRewarded(callback: () -> Unit) {
        this.userRewardedCallback = callback
    }
}
