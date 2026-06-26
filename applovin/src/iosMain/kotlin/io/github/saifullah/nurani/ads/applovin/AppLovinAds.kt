@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package io.github.saifullah.nurani.ads.applovin

import AppLovinSDK.ALMediationProviderMAX
import AppLovinSDK.ALSdk
import AppLovinSDK.ALSdkInitializationConfiguration
import io.github.saifullah.nurani.ads.core.AdInitResult
import io.github.saifullah.nurani.ads.core.compose.PlatformContext

actual object AppLovinAds {
    actual fun init(context: PlatformContext, androidSdkKey: String, iosSdkKey: String, onComplete: ((AdInitResult) -> Unit)?) {
        val configuration = ALSdkInitializationConfiguration.configurationWithSdkKey(androidSdkKey) { builder ->
            builder?.mediationProvider = ALMediationProviderMAX
        }
        ALSdk.shared().initializeWithConfiguration(configuration) {
            onComplete?.invoke(AdInitResult(true))
        }
    }

    actual fun isInitialized(): Boolean = ALSdk.shared().isInitialized()
}
