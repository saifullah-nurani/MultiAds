package io.github.saifullah.nurani.ads.core

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

@Stable
class AdStateManager(
    private val reloadPolicies: Set<AdReloadPolicy>,
    private val failedAdRetryRule: AdFailedRetryRule,
    refreshStrategy: AdRefreshStrategy,
    private val logger: AdLogger? = null,
    private val scheduler: Scheduler = Scheduler(),
    tag: String?,
    private val onLoadAd: () -> Unit,
) : DefaultAdLifecycleManager(), AdLoadCallback, AdContentCallback {
    private val tag: String = tag ?: "AdStateManager"
    private val maxAttempt = failedAdRetryRule.maxRetry
    private var refreshIntervalMillis: Long = -1
    private var preserveOnFailure: Boolean = true
    val shouldPreserveOnFailure get() = preserveOnFailure
    private var isRefreshingEnabled = false
    private var adLoadType: LoadType = LoadType.Usual
    private var _isAdAvailable by mutableStateOf(false)
    val isAdAvailable: Boolean get() = _isAdAvailable
    private var _isAdLoading by mutableStateOf(false)
    val isAdLoading: Boolean get() = _isAdLoading
    private var _isRetryingAdFailedLoad by mutableStateOf(false)
    val isRetryingAdFailedLoad get() = _isRetryingAdFailedLoad
    private var _isAdRefreshing by mutableStateOf(false)
    val isAdRefreshing: Boolean get() = _isAdRefreshing
    private var _isAdReloading by mutableStateOf(false)
    val isAdReloading: Boolean get() = _isAdReloading
    private var _isAdLoadScheduled by mutableStateOf(false)
    val isAdLoadScheduled: Boolean get() = _isAdLoadScheduled
    private var _attemptCount by mutableIntStateOf(0)
    val attemptCount: Int get() = _attemptCount

    private val adLoadRunnable: () -> Unit = {
        _isAdLoadScheduled = false
        loadAdIfNotLoading()
    }

    init {
        if (refreshStrategy is AdRefreshStrategy.Periodic) {
            refreshIntervalMillis = refreshStrategy.intervalMillis
            preserveOnFailure = refreshStrategy.preserveOnFailure
            isRefreshingEnabled = refreshIntervalMillis > 0
        }
    }


    fun loadAd() {
        _attemptCount = 0
        adLoadType = LoadType.Usual
        cancelSchedule("Manual Load")
        loadAdIfNotLoading()
    }

    fun loadAdIfIdle() {
        if (isAdAvailable || isAdLoading || isAdLoadScheduled) {
            logDebug("automatic load skipped (available/loading/scheduled)")
            return
        }
        _attemptCount = 0
        adLoadType = LoadType.Usual
        loadAdIfNotLoading()
    }

    private fun loadAdIfNotLoading() {
        if (isAdLoading) {
            logDebug("load skipped (already loading)")
            return
        }
        resetLoadState()
        _isAdLoading = true
        when (adLoadType) {
            LoadType.Usual -> {}
            LoadType.Reloading -> _isAdReloading = true
            LoadType.Refreshing -> _isAdRefreshing = true
            LoadType.FailedAd -> {
                _attemptCount++
                _isRetryingAdFailedLoad = true
            }
        }

        logger?.d("Starting $tag load (Type: $adLoadType, Attempt: $_attemptCount)")
        onLoadAd()
    }

    // -----------------------------
    // Load Callbacks
    // -----------------------------

    override fun onAdLoaded() {
        resetLoadState()
        _attemptCount = 0
        _isAdAvailable = true

        logDebug("loaded successfully")

        if (isRefreshingEnabled) {
            scheduleAdLoad(refreshIntervalMillis, "Refreshing", LoadType.Refreshing)
        }
    }

    override fun onAdFailedToLoad(error: AdError?) {

        // Note: we don't set _isAdAvailable to false here if preserveOnFailure is true
        // and an ad was already available.
        resetLoadState()
        logError("load failed attempt ${_attemptCount}/$maxAttempt error: $error")
        if (maxAttempt > 0 && _attemptCount < maxAttempt) {
            val delay = failedAdRetryRule.getDelayMillis(_attemptCount)
            logDebug("retry scheduled in $delay")
            scheduleAdLoad(delay, "Retry Attempt ${_attemptCount + 1}", LoadType.FailedAd)
        } else {
            logError("max retry attempts reached")
        }
    }

    private fun resetLoadState() {
        _isAdLoading = false
        _isAdRefreshing = false
        _isRetryingAdFailedLoad = false
        _isAdReloading = false
    }

    // -----------------------------
    // Ad Content Callbacks
    // -----------------------------

    override fun onAdDismissed() {
        logDebug("Ad dismissed")
        reloadAdWhen(AdReloadPolicy.OnDismissed)
    }

    override fun onAdClicked() {
        logDebug("clicked")
        reloadAdWhen(AdReloadPolicy.OnClicked)
    }

    override fun onAdFailedToShow(error: AdError?) {
        logDebug("failed to show $error")
        reloadAdWhen(AdReloadPolicy.OnFailedToShow)
    }

    override fun onAdDisplayed() {
        logDebug("displayed")
    }

    override fun onAdShowed() {
        logDebug("showed")
    }

    // -----------------------------
    // Lifecycle
    // -----------------------------

    override fun onDestroy() {
        cancelSchedule("Lifecycle Destroy")
    }

    override fun onStop() {
        cancelSchedule("Lifecycle Stop")
    }

    override fun onStart() {
        if (isRefreshingEnabled && !isAdLoading) {
            scheduleAdLoad(refreshIntervalMillis, "Lifecycle Start", LoadType.Refreshing)
        }
    }

    // -----------------------------
    // Reload Logic
    // -----------------------------

    private fun reloadAdWhen(policy: AdReloadPolicy) {

        if (!reloadPolicies.contains(policy)) return

        logDebug("reload triggered by $policy")

        cancelSchedule("Reload Policy")
        adLoadType = LoadType.Reloading
        loadAdIfNotLoading()
    }

    private fun scheduleAdLoad(
        delayMillis: Long,
        reason: String,
        type: LoadType = LoadType.Usual
    ) {
        cancelSchedule("Reschedule")
        adLoadType = type
        _isAdLoadScheduled = true
        if (type == LoadType.FailedAd) {
            _isRetryingAdFailedLoad = true
        }
        scheduler.schedule(delayMillis, adLoadRunnable)
        logDebug("scheduled load in $delayMillis reason: $reason type: $type")
    }

    private fun cancelSchedule(reason: String) {
        scheduler.cancel(adLoadRunnable)
        _isAdLoadScheduled = false
        logDebug("cancelled scheduled load reason: $reason")
    }

    private val localLogger: AdLogger = logger ?: io.github.saifullah.nurani.ads.core.utils.DefaultAdLogger(this.tag)

    private fun logDebug(msg: String) {
        localLogger.d(msg)
    }

    private fun logError(msg: String) {
        localLogger.e(msg)
    }

    private enum class LoadType {
        Usual, FailedAd, Reloading, Refreshing;
    }
}
