package io.github.saifullah.nurani.ads.pangle

import io.github.saifullah.nurani.ads.core.AdInitResult
import io.github.saifullah.nurani.ads.core.compose.PlatformContext

expect object PangleAds {
    fun init(
        context: PlatformContext,
        androidAppId: String,
        iosAppId: String,
        onComplete: ((AdInitResult) -> Unit)? = null
    )
    fun isInitialized(): Boolean
}
