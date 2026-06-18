@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package io.github.saifullah.nurani.ads.man

import FBAudienceNetwork.FBAudienceNetworkAds
import io.github.saifullah.nurani.ads.core.compose.PlatformContext

actual object MetaAds {
    actual fun init(context: PlatformContext, onComplete: ((Boolean) -> Unit)?) {
        FBAudienceNetworkAds.initializeWithSettings(null) { results ->
            onComplete?.invoke(results?.success ?: false)
        }
    }

    actual fun isInitialized(): Boolean = true
}
