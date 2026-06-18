package io.github.saifullah.nurani.ads.pangle.compose

import androidx.compose.runtime.Composable
import io.github.saifullah.nurani.ads.core.compose.LocalPlatformContext
import io.github.saifullah.nurani.ads.core.compose.PlatformContext

@Composable
expect fun rememberPangleAdsInit(
    androidAppId: String,
    iosAppId: String,
    context: PlatformContext = LocalPlatformContext.current
): Boolean
