package io.github.saifullah.nurani.ads.core

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun rememberBannerHeightController(
    expandWhenReady: Boolean,
    animateExpansion: Boolean
): BannerHeightController {
    return remember(expandWhenReady, animateExpansion) {
        BannerHeightController(
            expandWhenReady = expandWhenReady,
            animateExpansion = animateExpansion
        )
    }
}

@Stable
class BannerHeightController internal constructor(
    private val expandWhenReady: Boolean,
    private val animateExpansion: Boolean
) {
    private val _currentHeight = mutableStateOf(0)
    private val _showAdView = mutableStateOf(!expandWhenReady)

    val currentHeight: Int get() = _currentHeight.value
    val showAdView: Boolean get() = _showAdView.value

    fun onAdLoaded(height: Int) {
        _currentHeight.value = height
        if (expandWhenReady && !_showAdView.value) {
            _showAdView.value = true
        }
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
