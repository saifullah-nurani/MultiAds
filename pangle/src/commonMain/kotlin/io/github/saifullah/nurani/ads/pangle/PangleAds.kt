package io.github.saifullah.nurani.ads.pangle

import io.github.saifullah.nurani.ads.core.compose.PlatformContext

expect object PangleAds {
    fun init(context: PlatformContext, appId: String, onComplete: ((Boolean) -> Unit)? = null)
    fun isInitialized(): Boolean
}
