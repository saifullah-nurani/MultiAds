package io.github.saifullah.nurani.ads.multi.compose

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import androidx.compose.ui.unit.dp
import io.github.saifullah.nurani.ads.core.AdSize
import io.github.saifullah.nurani.ads.core.BannerAd
import io.github.saifullah.nurani.ads.core.BannerAdListener
import io.github.saifullah.nurani.ads.multi.MultiBannerUIView
import io.github.saifullah.nurani.ads.multi.models.WaterfallConfig
import kotlinx.cinterop.ExperimentalForeignApi

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun MultiBannerAd(
    waterfallConfig: WaterfallConfig,
    testModeEnabled: Boolean,
    adSize: BannerAd<AdSize>,
    adListener: BannerAdListener?
) {
    val heightDp = adSize.getSize().height
    val modifier = if (heightDp > 0) {
        Modifier.fillMaxWidth().height(heightDp.dp)
    } else {
        Modifier.fillMaxWidth()
    }

    UIKitView(
        modifier = modifier,
        factory = {
            MultiBannerUIView().apply {
                setWaterfallConfig(waterfallConfig)
                setTestModeEnabled(testModeEnabled)
                setBannerAd(adSize)
                setAdListener(adListener)
                loadAd()
            }
        },
        update = { view ->
            // Update if needed
        },
        onRelease = { view ->
            view.destroy()
        }
    )
}
