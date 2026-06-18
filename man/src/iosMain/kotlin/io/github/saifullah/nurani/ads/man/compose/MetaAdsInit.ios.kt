package io.github.saifullah.nurani.ads.man.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.github.saifullah.nurani.ads.core.compose.PlatformContext
import io.github.saifullah.nurani.ads.man.MetaAds

@Composable
actual fun rememberMetaAdsInit(context: PlatformContext): Boolean {
    var isInitialized by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        MetaAds.init(context) { success ->
            isInitialized = success
        }
    }
    return isInitialized
}
