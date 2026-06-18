package io.github.saifullah.nurani.ads.applovin

import io.github.saifullah.nurani.ads.core.compose.PlatformContext

expect object AppLovinAds {
    fun init(context: PlatformContext, sdkKey: String, onComplete: ((Boolean) -> Unit)? = null)
    fun isInitialized(): Boolean
}
