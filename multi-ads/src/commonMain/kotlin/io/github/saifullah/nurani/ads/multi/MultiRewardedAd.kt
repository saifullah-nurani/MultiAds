package io.github.saifullah.nurani.ads.multi

import io.github.saifullah.nurani.ads.core.AdLoadCallback
import io.github.saifullah.nurani.ads.core.AdContentCallback
import io.github.saifullah.nurani.ads.core.AdState
import io.github.saifullah.nurani.ads.core.compose.PlatformContext
import io.github.saifullah.nurani.ads.core.compose.PlatformActivity
import io.github.saifullah.nurani.ads.multi.models.MultiAdContentCallback
import io.github.saifullah.nurani.ads.multi.models.MultiAdLoadCallback
import io.github.saifullah.nurani.ads.multi.models.WaterfallConfig

expect class MultiRewardedAd(context: PlatformContext) : AdState {
    var waterfallConfig: WaterfallConfig?
    var testModeEnabled: Boolean
    var isImmersiveModeEnabled: Boolean
    var tag: String?

    fun showAd(activity: PlatformActivity)
    fun showAd(activity: PlatformActivity, onUserRewarded: () -> Unit)
    fun tryShowAd(): Boolean
    fun tryShowAd(onUserRewarded: () -> Unit): Boolean
    fun setOnUserRewarded(callback: () -> Unit)
    fun destroy()

    override val isAdAvailable: Boolean
    override val isAdLoading: Boolean
    override val isRetryingAdFailedLoad: Boolean
    override val isAdRefreshing: Boolean
    override val isAdReloading: Boolean
    override val attemptCount: Int

    override fun loadAd()
    override fun reloadAd()
    override fun setAdLoadCallback(callback: AdLoadCallback?)
    override fun setAdContentCallback(callback: AdContentCallback?)
    fun setMultiAdLoadCallback(callback: MultiAdLoadCallback?)
    fun setMultiAdContentCallback(callback: MultiAdContentCallback?)
}
