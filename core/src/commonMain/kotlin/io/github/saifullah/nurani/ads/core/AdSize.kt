package io.github.saifullah.nurani.ads.core

import androidx.compose.runtime.Immutable

/**
 * Represents the size of a banner advertisement.
 *
 * @property width The width of the ad in dp.
 * @property height The height of the ad in dp.
 */
@Immutable
class AdSize(
    val width: Int,
    val height: Int
) {

    override fun toString(): String {
        return "AdFormat(${width}x$height)"
    }

    companion object Companion {

        // ---------------------------------------------------------
        // Standard Banner Formats
        // ---------------------------------------------------------

        /** Standard Banner (320x50) */
        val BANNER = AdSize(320, 50)

        /** Large Banner (320x100) */
        val LARGE_BANNER = AdSize(320, 100)

        /** Medium Rectangle (300x250) */
        val MEDIUM_RECTANGLE = AdSize(300, 250)

        /** Leaderboard (728x90) */
        val LEADERBOARD = AdSize(728, 90)

        /** Full Banner (468x60) */
        val FULL_BANNER = AdSize(468, 60)

        /** Smart Banner (auto width x 50 height) */
        val SMART_BANNER = AdSize(-1, 50)

        /** Fluid / adaptive banner */
        val FLUID = AdSize(-1, -2)
    }
}