package io.github.saifullah.nurani.ads.vungle.compose

import androidx.compose.runtime.Immutable

@Immutable
data class VungleAdProperties internal constructor(
    val androidPlacementId: String,
    val iosPlacementId: String,
)

/**
 * Creates Vungle ad properties for Android and iOS placement IDs.
 */
fun vunglePlacementProperties(androidPlacementId: String, iosPlacementId: String): VungleAdProperties {
    return VungleAdProperties(androidPlacementId, iosPlacementId)
}
