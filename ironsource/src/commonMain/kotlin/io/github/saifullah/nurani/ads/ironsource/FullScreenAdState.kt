package io.github.saifullah.nurani.ads.ironsource

import io.github.saifullah.nurani.ads.core.AdConfig
import io.github.saifullah.nurani.ads.core.AdContentCallback
import io.github.saifullah.nurani.ads.core.AdLifecycleObserver
import io.github.saifullah.nurani.ads.core.AdLoadCallback
import io.github.saifullah.nurani.ads.core.AdLogger
import io.github.saifullah.nurani.ads.core.AdState
import io.github.saifullah.nurani.ads.core.AdStateManager
import io.github.saifullah.nurani.ads.core.Scheduler
import io.github.saifullah.nurani.ads.core.adConfig
import io.github.saifullah.nurani.ads.core.compose.PlatformActivity
import io.github.saifullah.nurani.ads.core.compose.PlatformContext

abstract class FullScreenAdState internal constructor(
    activity: PlatformActivity?,
    scheduler: Scheduler,
    adConfig: AdConfig?,
    tag: String?
) : AdState, AdLifecycleObserver {
    private var context: PlatformContext? = null
    private var activity: PlatformActivity? = null

    protected val adConfig: AdConfig
    protected val scheduler: Scheduler
    protected val tag: String?
    protected var adLogger: AdLogger? = null
    protected val adStateManager: AdStateManager
    protected var adLoadListener: AdLoadCallback? = null
    protected var adScreenContentCallback: AdContentCallback? = null
    override val isAdRefreshing: Boolean get() = adStateManager.isAdRefreshing
    override val isRetryingAdFailedLoad: Boolean get() = adStateManager.isRetryingAdFailedLoad
    override val isAdLoading: Boolean get() = adStateManager.isAdLoading
    override val isAdReloading: Boolean get() = adStateManager.isAdReloading
    override val attemptCount: Int get() = adStateManager.attemptCount
    var isImmersiveModeEnabled = false

    init {
        this.activity = activity
        this.adConfig = adConfig ?: adConfig()
        this.scheduler = scheduler
        this.tag = tag
        adLogger = this.adConfig.adLogger
        adStateManager = AdStateManager(
            reloadPolicies = this.adConfig.adReloadPolicies,
            failedAdRetryRule = this.adConfig.adFailedRetryRule,
            refreshStrategy = this.adConfig.adRefreshStrategy,
            logger = this.adConfig.adLogger,
            scheduler = scheduler,
            tag = tag,
            onLoadAd = ::onAdLoad
        )
    }

    constructor(
        platformContext: PlatformContext,
        scheduler: Scheduler,
        adConfig: AdConfig?,
        tag: String?
    ) : this(null, scheduler, adConfig, tag) {
        this.context = platformContext
    }

    abstract fun showAd(owner: PlatformActivity)
    abstract fun tryShowAd(): Boolean

    protected abstract fun onAdLoad()

    protected abstract fun clean()

    internal fun setContext(context: PlatformContext) {
        this.context = context
    }

    override fun reloadAd() {
        if (isAdLoading) return
        adStateManager.loadAd()
    }

    override fun setAdLoadCallback(callback: AdLoadCallback?) {
        this.adLoadListener = callback
    }

    override fun setAdContentCallback(callback: AdContentCallback?) {
        this.adScreenContentCallback = callback
    }

    override fun onDestroy() {
        adStateManager.onDestroy()
    }

    override fun onStart() {
        adStateManager.onStart()
    }

    override fun onStop() {
        adStateManager.onStop()
    }

}
