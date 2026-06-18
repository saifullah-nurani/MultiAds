package io.github.saifullah.nurani.ads.vungle

import io.github.saifullah.nurani.ads.core.compose.PlatformContext

expect object VungleAds {
    fun init(context: PlatformContext, appId: String, onComplete: ((Boolean) -> Unit)? = null)
    fun isInitialized(): Boolean
}
