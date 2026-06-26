package io.github.saifullah.nurani.ads.core

import androidx.compose.runtime.Immutable
import kotlin.random.Random
import kotlin.jvm.JvmName
import kotlin.jvm.JvmStatic
import kotlin.jvm.JvmSynthetic

/**
 * DSL marker to prevent accidental scope mixing when building [AdConfig].
 *
 * Example:
 *
 * adConfig {
 *     adLogger = MyLogger()
 * }
 */
@DslMarker
annotation class AdConfigDsl


/**
 * Entry point function for building an [AdConfig] using Kotlin DSL.
 *
 * Example:
 *
 * val config = adConfig {
 *     isTestModeEnabled = true
 * }
 */
@JvmSynthetic
fun adConfig(
    block: AdConfigBuilder.() -> Unit = {}
): AdConfig = AdConfigBuilder().apply(block).build()


/**
 * Builder used internally by the DSL to construct an immutable [AdConfig].
 *
 * This builder collects all configuration values and produces the final
 * configuration object via [build].
 */
@AdConfigDsl
class AdConfigBuilder internal constructor() {

    /**
     * Optional logger used by the ad system to log lifecycle
     * events, errors, and debug messages.
     *
     * If null, logging will be disabled.
     */
    var adLogger: AdLogger? = null

    /**
     * Policies that determine when an ad should automatically reload.
     *
     * Default policies:
     * - OnDismissed
     * - OnFailedToShow
     */
    @JvmSynthetic
    var adReloadPolicies: Set<AdReloadPolicy> = setOf(
        AdReloadPolicy.OnDismissed,
        AdReloadPolicy.OnFailedToShow
    )

    /**
     * Retry strategy applied when an ad fails to load.
     *
     * By default an exponential retry strategy is used.
     */
    @JvmSynthetic
    var adFailedRetryRule: AdFailedRetryRule = exponentialRetry()

    /**
     * Strategy controlling periodic ad refresh behaviour.
     *
     * Default implementation enables periodic refresh.
     */
    @JvmSynthetic
    var adRefreshStrategy: AdRefreshStrategy = periodicRefresh()

    /**
     * Optional custom tag attached to ad requests when supported by the
     * underlying SDK. Provide your own unique value if you want to correlate
     * requests with server-side verification.
     */
    @JvmSynthetic
    var tag: String? = null
    /**
     * Enables test mode for ads.
     *
     * When enabled, test ad unit IDs will be used instead of
     * production ad unit IDs.
     */
    @JvmSynthetic
    var isTestModeEnabled: Boolean = false


    /**
     * Builds and returns the final immutable [AdConfig].
     *
     * This method converts mutable builder fields into immutable
     * values used by the ad system.
     */
    internal fun build(): AdConfig {
        return AdConfig(
            adLogger = adLogger,
            adReloadPolicies = adReloadPolicies.toSet(),
            adFailedRetryRule = adFailedRetryRule,
            adRefreshStrategy = adRefreshStrategy,
            tag = tag,
            isTestModeEnabled = isTestModeEnabled
        )
    }
}


/**
 * Immutable configuration object used by the ad system.
 *
 * This configuration controls:
 * - logging behaviour
 * - reload policies
 * - retry rules
 * - refresh strategies
 * - test mode
 */
@Immutable
data class AdConfig(

    /**
     * Logger used for debugging and internal SDK logs.
     */
    val adLogger: AdLogger?,

    /**
     * Set of reload policies determining when an ad should reload.
     */
    val adReloadPolicies: Set<AdReloadPolicy>,

    /**
     * Retry rule applied when an ad fails to load.
     */
    val adFailedRetryRule: AdFailedRetryRule,

    /**
     * Strategy defining how and when ads should refresh.
     */
    val adRefreshStrategy: AdRefreshStrategy,

    /**
     * Optional custom tag carried with ad requests when supported.
     */
    val tag: String?,

    /**
     * Indicates whether the SDK should operate in test mode.
     */
    val isTestModeEnabled: Boolean
) {
    companion object {
        @JvmStatic
        val default = adConfig()

        @JvmStatic
        fun newVerificationTag(prefix: String = "ad"): String {
            val randomPart = Random.nextLong().toULong().toString(16)
            return "$prefix-$randomPart"
        }
    }
}
