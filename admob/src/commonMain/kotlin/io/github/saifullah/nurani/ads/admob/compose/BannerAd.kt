package io.github.saifullah.nurani.ads.admob.compose

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.saifullah.nurani.ads.core.AdFailedRetryRule
import io.github.saifullah.nurani.ads.core.AdLogger
import io.github.saifullah.nurani.ads.core.AdReloadPolicy
import io.github.saifullah.nurani.ads.core.AdSize
import io.github.saifullah.nurani.ads.core.BannerAdListener
import io.github.saifullah.nurani.ads.core.BannerAd

/**
 * Remembers and manages the lifecycle of a BannerAd.
 *
 * This function integrates with Compose lifecycle to automatically:
 * - start and stop the ad state
 * - load the ad initially if required
 * - attach load/content callbacks
 */
@Composable
expect fun AdmobBannerAd(
    properties: AdmobAdProperties,
    testModeEnabled: Boolean = false,
    expandWhenReady: Boolean = false,
    animateExpansion: Boolean = true,
    adSize: BannerAd<AdSize> = AdmobDefault.DefaultBannerAd,
    adFailedAdRetryRule: AdFailedRetryRule = AdmobDefault.DefaultAdFailedRetryRule,
    adReloadPolicies: Set<AdReloadPolicy> = AdmobDefault.DefaultAdReloadPolicy,
    adLogger: AdLogger? = null,
    adListener: BannerAdListener? = null
)


@Composable
internal fun rememberBannerHeightController(
    expandWhenReady: Boolean,
    animateExpansion: Boolean
): BannerHeightController {

    return remember(expandWhenReady, animateExpansion) {
        BannerHeightController(
            expandWhenReady,
            animateExpansion
        )
    }
}
@Stable
internal class BannerHeightController(
    private val expandWhenReady: Boolean,
    private val animateExpansion: Boolean
) {

    private val _currentHeight = mutableStateOf(0)
    private val _showAdView = mutableStateOf(!expandWhenReady)

    val currentHeight: Int get() = _currentHeight.value
    val showAdView: Boolean get() = _showAdView.value

    fun onAdLoaded(height: Int) {
        _currentHeight.value = height
    }

    fun onAdDisplayed() {
        if (expandWhenReady && !_showAdView.value) {
            _showAdView.value = true
        }
    }

    @Composable
    fun animatedHeight(): Dp {
        val targetHeight = if (showAdView) currentHeight.dp else 0.dp

        val animated by animateDpAsState(
            targetValue = targetHeight,
            label = "BannerHeight"
        )

        return if (animateExpansion) animated else targetHeight
    }
}