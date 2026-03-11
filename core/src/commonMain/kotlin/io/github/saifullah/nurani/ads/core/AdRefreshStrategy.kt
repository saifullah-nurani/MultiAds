package io.github.saifullah.nurani.ads.core

import androidx.compose.runtime.Immutable
import kotlin.jvm.JvmStatic

/**
 * DSL marker used to restrict nested DSL scope
 * for refresh strategy configuration.
 */
@DslMarker
private annotation class AdRefreshStrategyDsl


/**
 * Disables automatic ad refresh.
 *
 * Example:
 * ```
 * val refresh = disableRefresh()
 * ```
 */
fun disableRefresh(): AdRefreshStrategy = AdRefreshStrategy.Disabled

/**
 * Creates a periodic refresh strategy using Kotlin DSL.
 *
 * Example:
 * ```
 * val refresh = periodicRefresh {
 *     intervalMillis = 180000
 *     preserveOnFailure = true
 * }
 * ```
 */
fun periodicRefresh(
    block: PeriodicRefreshBuilder.() -> Unit={}
): AdRefreshStrategy = PeriodicRefreshBuilder().apply(block).build()


/**
 * DSL builder for creating a periodic refresh strategy.
 *
 * This allows configuring how often ads should refresh
 * and whether refresh should continue after failures.
 */
@AdRefreshStrategyDsl
class PeriodicRefreshBuilder internal constructor() {

    /**
     * Refresh interval in milliseconds.
     *
     * Default: 30 seconds
     */
    var intervalMillis: Long = AdRefreshStrategy.DEFAULT_INTERVAL_MS

    /**
     * Whether refresh should continue after a failed load.
     *
     * If true, refresh attempts continue even if loading fails.
     */
    var preserveOnFailure: Boolean =
        AdRefreshStrategy.DEFAULT_PRESERVE_ON_FAILURE

    /**
     * Builds the refresh strategy.
     */
    internal fun build(): AdRefreshStrategy {
        return AdRefreshStrategy.Periodic(
            intervalMillis = intervalMillis.coerceAtLeast(0),
            preserveOnFailure = preserveOnFailure
        )
    }
}


/**
 * Represents a strategy for refreshing ads automatically.
 *
 * Two strategies are available:
 *
 * 1. [Periodic] – refresh ads at a fixed interval
 * 2. [Disabled] – disable automatic refresh
 */
@Immutable
sealed class AdRefreshStrategy {

    /**
     * Periodic refresh strategy.
     *
     * Ads will be refreshed automatically
     * after the specified interval.
     *
     * @param intervalMillis refresh interval in milliseconds
     * @param preserveOnFailure whether refresh continues after failure
     */
    class Periodic internal constructor(
        val intervalMillis: Long,
        val preserveOnFailure: Boolean
    ) : AdRefreshStrategy()


    /**
     * Refresh disabled.
     *
     * Ads will only load manually.
     */
    object Disabled : AdRefreshStrategy()


    companion object {

        /**
         * Default refresh interval (180 seconds).
         */
        const val DEFAULT_INTERVAL_MS: Long = 180000

        /**
         * Default behavior when refresh fails.
         */
        const val DEFAULT_PRESERVE_ON_FAILURE: Boolean = true

        @JvmStatic
        fun disable(): AdRefreshStrategy = disableRefresh()

        @JvmStatic
        fun periodic(): AdRefreshStrategy = periodicRefresh()
    }
}