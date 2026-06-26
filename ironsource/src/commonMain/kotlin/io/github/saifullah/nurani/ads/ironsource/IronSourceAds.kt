package io.github.saifullah.nurani.ads.ironsource

import io.github.saifullah.nurani.ads.core.AdInitResult
import io.github.saifullah.nurani.ads.core.compose.PlatformContext

expect object IronSourceAds {
    fun init(
        context: PlatformContext,
        androidAppKey: String,
        iosAppKey: String,
        onComplete: ((AdInitResult) -> Unit)? = null
    )
    fun isInitialized(): Boolean
}
