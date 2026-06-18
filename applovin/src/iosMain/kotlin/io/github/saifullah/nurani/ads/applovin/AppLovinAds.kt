@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package io.github.saifullah.nurani.ads.applovin

import AppLovinSDK.ALMediationProviderMAX
import AppLovinSDK.ALSdk
import AppLovinSDK.ALSdkInitializationConfiguration
import io.github.saifullah.nurani.ads.core.compose.PlatformContext

actual object AppLovinAds {
    actual fun init(context: PlatformContext, sdkKey: String, onComplete: ((Boolean) -> Unit)?) {
        val configuration = ALSdkInitializationConfiguration.configurationWithSdkKey(sdkKey) { builder ->
            builder?.mediationProvider = ALMediationProviderMAX
        }
        ALSdk.shared().initializeWithConfiguration(configuration) {
            onComplete?.invoke(true)
        }
    }

    actual fun isInitialized(): Boolean = ALSdk.shared().isInitialized()
}
