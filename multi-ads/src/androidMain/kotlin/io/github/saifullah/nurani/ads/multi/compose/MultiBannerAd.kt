package io.github.saifullah.nurani.ads.multi.compose

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import io.github.saifullah.nurani.ads.core.AdSize
import io.github.saifullah.nurani.ads.core.BannerAd
import io.github.saifullah.nurani.ads.core.BannerAdListener
import io.github.saifullah.nurani.ads.multi.MultiBannerView
import io.github.saifullah.nurani.ads.multi.models.WaterfallConfig

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

    AndroidView(
        modifier = modifier,
        factory = { context ->
            MultiBannerView(context).apply {
                setWaterfallConfig(waterfallConfig)
                setTestModeEnabled(testModeEnabled)
                setBannerAd(adSize)
                setAdListener(adListener)
                loadAd()
            }
        },
        update = { view ->
            // Update if needed, though usually banners reload manually
        },
        onRelease = { view ->
            view.destroy()
        }
    )
}
