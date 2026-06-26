@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package io.github.saifullah.nurani.ads.pangle

import PAGAdSDK.PAGConfig
import PAGAdSDK.PAGSdk
import io.github.saifullah.nurani.ads.core.AdInitResult

actual object PangleAds {
    private var isInitialized = false

    fun init(appId: String, onComplete: ((Boolean) -> Unit)? = null) {
        val config = PAGConfig.shareConfig()
        config.appID = appId
        PAGSdk.startWithConfig(config) { success, error ->
            if (success) {
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
