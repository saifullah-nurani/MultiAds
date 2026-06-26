package io.github.saifullah.nurani.ads.inmobi.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.github.saifullah.nurani.ads.core.AdInitResult
import io.github.saifullah.nurani.ads.inmobi.InMobiAds
import io.github.saifullah.nurani.ads.core.compose.PlatformContext

@Composable
actual fun rememberInMobiAdsInit(
    androidAccountId: String,
    iosAccountId: String,
    context: PlatformContext
): Boolean {
    var isInitialized by remember(iosAccountId) { mutableStateOf(false) }
    LaunchedEffect(iosAccountId) {
        InMobiAds.init(context, androidAccountId, iosAccountId) { result: AdInitResult ->
            isInitialized = result.success
        }
    }
    return isInitialized
}
