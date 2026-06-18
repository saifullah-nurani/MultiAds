package io.github.saifullah.nurani.ads.man

import io.github.saifullah.nurani.ads.core.AdConfig
import io.github.saifullah.nurani.ads.core.Scheduler
import io.github.saifullah.nurani.ads.core.compose.PlatformActivity
import io.github.saifullah.nurani.ads.core.compose.PlatformContext

abstract class MetaRewardedAdState(
    activity: PlatformActivity?,
    scheduler: Scheduler,
    adConfig: AdConfig?,
    tag: String?
) : MetaFullScreenAdState(activity, scheduler, adConfig, tag) {
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
