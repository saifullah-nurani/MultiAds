package io.github.saifullah.nurani.ads.inmobi

import io.github.saifullah.nurani.ads.core.compose.PlatformContext

expect object InMobiAds {
    fun init(context: PlatformContext, accountId: String, onComplete: ((Boolean) -> Unit)? = null)
    fun isInitialized(): Boolean
}
