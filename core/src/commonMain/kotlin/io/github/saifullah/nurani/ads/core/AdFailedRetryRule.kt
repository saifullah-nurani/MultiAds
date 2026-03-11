package io.github.saifullah.nurani.ads.core

import kotlin.jvm.JvmStatic
import kotlin.math.pow

/**
 * DSL marker used to restrict nested DSL scopes
 * for retry rule configuration.
 */
@DslMarker
private annotation class AdFailedRetryDsl


/**
 * Returns a retry rule that disables retry completely.
 *
 * Usage:
 * ```
 * val retry = noRetry()
 * ```
 */
fun noRetry(): AdFailedRetryRule = AdFailedRetryRule.None


/**
 * Creates a linear retry rule using Kotlin DSL.
 *
 * Example:
 * ```
 * val retry = linearRetry {
 *     delayInMillis = 3000
 *     maxAttempts = 5
 * }
 * ```
 */
fun linearRetry(block: LinearRetryBuilder.() -> Unit={}): AdFailedRetryRule =
    LinearRetryBuilder().apply(block).build()

/**
 * Creates an exponential retry rule using Kotlin DSL.
 *
 * Example:
 * ```
 * val retry = exponentialRetry {
 *     delayInMillis = 2000
 *     maxAttempts = 5
 *     multiplier = 2f
 * }
 * ```
 */
fun exponentialRetry(block: ExponentialRetryBuilder.() -> Unit={}): AdFailedRetryRule =
    ExponentialRetryBuilder().apply(block).build()


/**
 * DSL builder for creating a [AdFailedRetryRule.Linear] retry strategy.
 *
 * This strategy retries with a constant delay between attempts.
 *
 * Example delay pattern:
 * ```
 * 2s → 2s → 2s → 2s
 * ```
 */
@AdFailedRetryDsl
class LinearRetryBuilder internal constructor() {

    /**
     * Maximum number of retry attempts.
     */
    var maxAttempts: Int = AdFailedRetryRule.DEFAULT_MAX_ATTEMPTS

    /**
     * Delay between retry attempts in milliseconds.
     */
    var delayInMillis: Long = AdFailedRetryRule.DEFAULT_DELAY_MS

    /**
     * Builds the retry rule.
     */
    internal fun build(): AdFailedRetryRule {
        return AdFailedRetryRule.Linear(delayInMillis, maxAttempts)
    }
}


/**
 * DSL builder for creating a [AdFailedRetryRule.Exponential] retry strategy.
 *
 * This strategy increases delay exponentially after each retry.
 *
 * Example delay pattern (delay=2s, multiplier=2):
 * ```
 * 2s → 4s → 8s → 16s
 * ```
 */
@AdFailedRetryDsl
class ExponentialRetryBuilder internal constructor() {

    /**
     * Exponential multiplier applied after each retry.
     */
    var multiplier: Float = AdFailedRetryRule.DEFAULT_MULTIPLIER

    /**
     * Maximum number of retry attempts.
     */
    var maxAttempts: Int = AdFailedRetryRule.DEFAULT_MAX_ATTEMPTS

    /**
     * Initial delay before retrying.
     */
    var delayInMillis: Long = AdFailedRetryRule.DEFAULT_DELAY_MS

    /**
     * Builds the retry rule.
     */
    internal fun build(): AdFailedRetryRule {
        return AdFailedRetryRule.Exponential(delayInMillis, maxAttempts, multiplier)
    }
}


/**
 * Represents a retry strategy used when an ad fails to load.
 *
 * This sealed class provides different retry policies:
 *
 * 1. [None] – No retry.
 * 2. [Linear] – Retry with constant delay.
 * 3. [Exponential] – Retry with exponentially increasing delay.
 */
sealed class AdFailedRetryRule {

    /**
     * Returns the delay in milliseconds before the next retry attempt.
     *
     * @param attempt current retry attempt number (starting from 1)
     */
    internal open fun getDelayMillis(attempt: Int): Long {
        return 0
    }

    /**
     * Maximum number of retry attempts allowed.
     */
    internal open val maxRetry: Int = DEFAULT_MAX_ATTEMPTS

    /**
     * Base delay used by retry strategies.
     */
    internal open val delayMillis: Long = DEFAULT_DELAY_MS


    /**
     * Retry strategy that disables retries completely.
     */
    internal object None : AdFailedRetryRule()


    /**
     * Linear retry strategy.
     *
     * Delay between retries remains constant.
     *
     * Example:
     * ```
     * delay → delay → delay
     * ```
     */
    internal class Linear internal constructor(
        override val delayMillis: Long,
        override val maxRetry: Int
    ) : AdFailedRetryRule() {

        override fun getDelayMillis(attempt: Int): Long {
            return delayMillis
        }
    }


    /**
     * Exponential retry strategy.
     *
     * Delay grows exponentially based on multiplier.
     *
     * Example:
     * ```
     * delay * multiplier^(attempt-1)
     * ```
     */
    internal class Exponential internal constructor(
        override val delayMillis: Long,
        override val maxRetry: Int,
        val multiplier: Float = DEFAULT_MULTIPLIER,
    ) : AdFailedRetryRule() {

        override fun getDelayMillis(attempt: Int): Long {
            return (delayMillis * multiplier.toDouble().pow((attempt - 1).toDouble())).toLong()
        }
    }


    companion object {

        /**
         * Default base delay (2 seconds).
         */
        const val DEFAULT_DELAY_MS: Long = 2000

        /**
         * Default maximum retry attempts.
         */
        const val DEFAULT_MAX_ATTEMPTS: Int = 3

        /**
         * Default exponential multiplier.
         */
        const val DEFAULT_MULTIPLIER: Float = 2.0f

        @JvmStatic
        fun exponentialDefault(): AdFailedRetryRule= exponentialRetry()

        @JvmStatic
        fun linearDefault(): AdFailedRetryRule= linearRetry()

        @JvmStatic
        fun none(): AdFailedRetryRule= noRetry()
    }
}