package io.github.saifullah.nurani.ads.inmobi.compose

import androidx.compose.runtime.Composable
import io.github.saifullah.nurani.ads.core.compose.LocalPlatformContext
import io.github.saifullah.nurani.ads.core.compose.PlatformContext

@Composable
expect fun rememberInMobiAdsInit(
    androidAccountId: String,
    iosAccountId: String,
    context: PlatformContext = LocalPlatformContext.current
): Boolean
