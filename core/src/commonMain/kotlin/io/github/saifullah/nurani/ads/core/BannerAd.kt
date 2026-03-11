package io.github.saifullah.nurani.ads.core


/**
 * DSL marker used to restrict nested banner size DSL scopes.
 */
@DslMarker
private annotation class BannerAdDsl


/**
 * Creates a fixed banner ad size strategy.
 *
 * Example:
 * ```
 * val size = fixedBannerAd {
 *     adSize = AdSize.BANNER
 * }
 * ```
 */
fun <T> fixedBannerAd(
    block: FixedAdSizeBuilder<T>.() -> Unit = {}
): BannerAd<T> = FixedAdSizeBuilder<T>().apply(block).build()

/**
 * DSL builder for creating a fixed banner size strategy.
 */
@BannerAdDsl
class FixedAdSizeBuilder<T> internal constructor() {

    /**
     * The banner size that will always be returned.
     */
    var adSize: T? = null

    internal fun build(): BannerAd<T> {

        val size = requireNotNull(adSize) {
            "adSize must be provided."
        }

        return BannerAd.Fixed(size)
    }
}


/**
 * Creates a random banner ad size strategy.
 *
 * Example:
 * ```
 * val size = randomBannerAd {
 *     adSizes = setOf(
 *         AdSize.BANNER,
 *         AdSize.LARGE_BANNER,
 *         AdSize.MEDIUM_RECTANGLE
 *     )
 * }
 * ```
 */
fun <T> randomBannerAd(
    block: RandomAdSizeBuilder<T>.() -> Unit = {}
): BannerAd<T> = RandomAdSizeBuilder<T>().apply(block).build()


/**
 * DSL builder for creating a random banner size strategy.
 */
@BannerAdDsl
class RandomAdSizeBuilder<T> internal constructor() {

    /**
     * Banner sizes that can be randomly selected.
     */
    var adSizes: Set<T> = emptySet()

    internal fun build(): BannerAd<T> {

        require(adSizes.isNotEmpty()) {
            "At least one banner size must be provided."
        }

        return BannerAd.Random(adSizes)
    }
}

/**
 * Represents a strategy used to determine which banner ad size should be used.
 *
 * Implementations:
 *
 * 1. [Fixed] → Always return the same banner size
 * 2. [Random] → Randomly choose from a list of banner sizes
 *
 * @param T banner size type (e.g. AdSize)
 */
sealed class BannerAd<out T> {

    companion object {

        /**
         * Creates a fixed banner size strategy.
         */
        fun <T> fixed(size: T): BannerAd<T> {
            return Fixed(size)
        }

        /**
         * Creates a random banner size strategy.
         */
        fun <T> random(sizes: Set<T>): BannerAd<T> {
            return Random(sizes)
        }
    }

    /**
     * Returns the banner size according to the strategy.
     */
    open fun getSize(): T {
        error("BannerAd strategy must implement getSize()")
    }

    /**
     * Converts the currently selected banner size to another type.
     *
     * Useful for converting SDK-specific banner sizes.
     *
     * Example:
     * ```
     * val adSize = bannerSize.mapTo {
     *     AdSize(it.width, it.height)
     * }
     * ```
     */
    open fun <R> mapTo(transform: (T) -> R): R {
        return transform(getSize())
    }

    /**
     * Converts the entire banner size strategy to another type.
     *
     * This preserves the strategy behavior while converting
     * the underlying banner size type.
     *
     * Example:
     * ```
     * val sizes = bannerSize.mapToBannerAd {
     *     AdSize(it.width, it.height)
     * }
     * ```
     */
    open fun <R> mapToBannerAd(transform: (T) -> R): BannerAd<R> {
        error("BannerAd strategy must implement mapToBannerAd()")
    }

    /**
     * Fixed banner size strategy.
     *
     * Always returns the same banner size.
     */
    class Fixed<out T>(
        private val size: T
    ) : BannerAd<T>() {

        /**
         * Returns the configured banner size.
         */
        override fun getSize(): T = size

        override fun <R> mapToBannerAd(transform: (T) -> R): BannerAd<R> {
            return Fixed(transform(size))
        }
    }

    /**
     * Random banner size strategy.
     *
     * Selects a banner size randomly from the provided set.
     */
    class Random<out T>(
        sizes: Set<T>
    ) : BannerAd<T>() {

        private val sizes = sizes.toList()
        private val random = kotlin.random.Random.Default

        constructor(vararg sizes: T) : this(sizes.toHashSet())

        /**
         * Returns a randomly selected banner size.
         */
        override fun getSize(): T {
            return sizes[random.nextInt(sizes.size)]
        }

        override fun <R> mapToBannerAd(transform: (T) -> R): Random<R> {
            return Random(sizes.map(transform).toSet())
        }
    }
}
