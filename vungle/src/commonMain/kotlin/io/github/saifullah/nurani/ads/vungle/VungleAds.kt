package io.github.saifullah.nurani.ads.vungle

import io.github.saifullah.nurani.ads.core.AdInitResult
import io.github.saifullah.nurani.ads.core.compose.PlatformContext

expect object VungleAds {
    fun init(
        context: PlatformContext,
        androidAppId: String,
        iosAppId: String,
        onComplete: ((AdInitResult) -> Unit)? = null
    )
    fun isInitialized(): Boolean
}
