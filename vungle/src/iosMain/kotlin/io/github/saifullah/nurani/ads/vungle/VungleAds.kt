@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package io.github.saifullah.nurani.ads.vungle

import VungleAdsSDK.VungleAds
import io.github.saifullah.nurani.ads.core.AdInitResult
import platform.Foundation.NSError

actual object VungleAds {
    private var isInitialized = false

    fun init(appId: String, onComplete: ((Boolean) -> Unit)? = null) {
        VungleAds.initWithAppId(appId) { error: NSError? ->
            if (error == null) {
                isInitialized = true
                onComplete?.invoke(true)
            } else {
                onComplete?.invoke(false)
            }
        }
    }

    actual fun init(
        context: io.github.saifullah.nurani.ads.core.compose.PlatformContext,
        androidAppId: String,
        iosAppId: String,
        onComplete: ((AdInitResult) -> Unit)?
    ) {
        init(iosAppId) { success ->
            onComplete?.invoke(AdInitResult(success))
        }
    }

    actual fun isInitialized(): Boolean = isInitialized
}
