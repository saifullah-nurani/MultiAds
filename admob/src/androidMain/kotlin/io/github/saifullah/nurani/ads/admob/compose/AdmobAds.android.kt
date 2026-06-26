package io.github.saifullah.nurani.ads.admob.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.github.saifullah.nurani.ads.admob.AdmobAds
import io.github.saifullah.nurani.ads.core.compose.PlatformContext

@Composable
actual fun rememberAdmobAdsInit(androidAppId: String, iosAppId: String, context: PlatformContext): Boolean {
    var isInitialized by remember(context) { mutableStateOf(false) }
    LaunchedEffect(context) {
        AdmobAds.init(context)
        isInitialized = true
    }
    return isInitialized
}
