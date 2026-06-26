package io.github.saifullah.nurani.ads.man

import io.github.saifullah.nurani.ads.core.AdInitResult
import io.github.saifullah.nurani.ads.core.compose.PlatformContext

expect object MetaAds {
    fun init(
        context: PlatformContext,
        androidPlacementId: String,
        iosPlacementId: String,
        onComplete: ((AdInitResult) -> Unit)? = null
    )
    fun isInitialized(): Boolean
}
