package io.github.saifullah.nurani.ads.admob.compose

import androidx.compose.runtime.Composable
import io.github.saifullah.nurani.ads.core.compose.LocalPlatformContext
import io.github.saifullah.nurani.ads.core.compose.PlatformContext

@Composable
expect fun rememberAdmobAdsInit(
    androidAppId: String,
    iosAppId: String = androidAppId,
    context: PlatformContext = LocalPlatformContext.current
): Boolean
