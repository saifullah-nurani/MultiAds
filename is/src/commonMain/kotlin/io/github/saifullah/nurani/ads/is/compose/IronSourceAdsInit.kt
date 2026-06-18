package io.github.saifullah.nurani.ads.`is`.compose

import androidx.compose.runtime.Composable
import io.github.saifullah.nurani.ads.core.compose.LocalPlatformContext
import io.github.saifullah.nurani.ads.core.compose.PlatformContext

@Composable
expect fun rememberIronSourceAdsInit(
    androidAppKey: String,
    iosAppKey: String,
    testModeEnabled: Boolean = false,
    context: PlatformContext = LocalPlatformContext.current
): Boolean
