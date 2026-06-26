package io.github.saifullah.nurani.ads.inmobi

import io.github.saifullah.nurani.ads.core.AdInitResult
import io.github.saifullah.nurani.ads.core.compose.PlatformContext

expect object InMobiAds {
    fun init(
        context: PlatformContext,
        androidAccountId: String,
        iosAccountId: String,
        onComplete: ((AdInitResult) -> Unit)? = null
    )
    fun isInitialized(): Boolean
}
