package io.github.saifullah.nurani.ads.applovin.compose

import androidx.compose.runtime.Composable
import io.github.saifullah.nurani.ads.core.compose.LocalPlatformContext
import io.github.saifullah.nurani.ads.core.compose.PlatformContext

@Composable
expect fun rememberAppLovinAdsInit(
    androidSdkKey: String,
    iosSdkKey: String,
    context: PlatformContext = LocalPlatformContext.current
): Boolean
