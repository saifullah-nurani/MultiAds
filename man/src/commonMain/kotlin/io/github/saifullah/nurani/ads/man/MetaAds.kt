package io.github.saifullah.nurani.ads.man

import io.github.saifullah.nurani.ads.core.compose.PlatformContext

expect object MetaAds {
    fun init(context: PlatformContext, onComplete: ((Boolean) -> Unit)? = null)
    fun isInitialized(): Boolean
}
