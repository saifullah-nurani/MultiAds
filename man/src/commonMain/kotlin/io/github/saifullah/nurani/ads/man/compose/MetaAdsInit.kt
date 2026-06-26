package io.github.saifullah.nurani.ads.man.compose

import androidx.compose.runtime.Composable
import io.github.saifullah.nurani.ads.core.compose.LocalPlatformContext
import io.github.saifullah.nurani.ads.core.compose.PlatformContext

@Composable
expect fun rememberMetaAdsInit(
    androidPlacementId: String,
    iosPlacementId: String = androidPlacementId,
    context: PlatformContext = LocalPlatformContext.current
): Boolean
