package io.github.saifullah.nurani.ads.inmobi.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.github.saifullah.nurani.ads.inmobi.InMobiAds
import io.github.saifullah.nurani.ads.core.compose.PlatformContext

@Composable
actual fun rememberInMobiAdsInit(
    androidAccountId: String,
    iosAccountId: String,
    context: PlatformContext
): Boolean {
    var isInitialized by remember(androidAccountId) { mutableStateOf(false) }
    LaunchedEffect(androidAccountId, context) {
        InMobiAds.init(context, androidAccountId) { success: Boolean ->
            isInitialized = success
        }
    }
    return isInitialized
}
