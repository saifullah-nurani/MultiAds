package io.github.saifullah.nurani.ads.multi

import io.github.saifullah.nurani.ads.core.AdLoadCallback
import io.github.saifullah.nurani.ads.core.AdContentCallback
import io.github.saifullah.nurani.ads.core.AppOpenAd
import io.github.saifullah.nurani.ads.core.compose.PlatformContext
import io.github.saifullah.nurani.ads.core.compose.PlatformActivity
import io.github.saifullah.nurani.ads.multi.models.MultiAdContentCallback
import io.github.saifullah.nurani.ads.multi.models.MultiAdLoadCallback
import io.github.saifullah.nurani.ads.multi.models.WaterfallConfig

expect class MultiAppOpenAd(context: PlatformContext) : AppOpenAd {
    var waterfallConfig: WaterfallConfig?
    var testModeEnabled: Boolean
    var isImmersiveModeEnabled: Boolean
    var tag: String?

    override fun showAd(activity: PlatformActivity)
    override fun tryShowAd(): Boolean
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
