package io.github.saifullah.nurani.ads.`is`

import io.github.saifullah.nurani.ads.core.compose.PlatformContext

expect object IronSourceAds {
    fun init(context: PlatformContext, appKey: String, onComplete: ((Boolean) -> Unit)? = null)
    fun isInitialized(): Boolean
}
