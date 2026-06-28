package io.github.saifullah.nurani.ads.man

import io.github.saifullah.nurani.ads.core.AdInitResult
import io.github.saifullah.nurani.ads.core.compose.PlatformContext

expect object MetaAudienceNetworkAds {
    fun init(
        context: PlatformContext,
        onComplete: ((AdInitResult) -> Unit)? = null
    )
    fun isInitialized(): Boolean
}
