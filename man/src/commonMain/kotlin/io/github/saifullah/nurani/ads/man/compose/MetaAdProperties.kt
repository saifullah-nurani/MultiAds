package io.github.saifullah.nurani.ads.man.compose

import androidx.compose.runtime.Immutable

@Immutable
data class MetaAdProperties internal constructor(
    val androidPlacementId: String,
    val iosPlacementId: String,
)

/**
 * Creates Meta ad properties for Android and iOS placement IDs.
 */
fun metaPlacementProperties(androidPlacementId: String, iosPlacementId: String): MetaAdProperties {
    return MetaAdProperties(androidPlacementId, iosPlacementId)
}
