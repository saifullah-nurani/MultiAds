package io.github.saifullah.nurani.ads.applovin

import io.github.saifullah.nurani.ads.core.AdInitResult
import io.github.saifullah.nurani.ads.core.compose.PlatformContext

expect object AppLovinAds {
    fun init(
        context: PlatformContext,
        androidSdkKey: String,
        iosSdkKey: String,
        onComplete: ((AdInitResult) -> Unit)? = null
    )
    fun isInitialized(): Boolean
}
