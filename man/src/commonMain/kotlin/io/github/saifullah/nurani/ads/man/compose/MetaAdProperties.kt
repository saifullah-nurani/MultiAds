package io.github.saifullah.nurani.ads.man.compose

import androidx.compose.runtime.Immutable

@Immutable
data class MetaAdProperties internal constructor(
    val androidPlacementId: String,
    val iosPlacementId: String,
    val tag: String? = null,
)

/**
 * Creates Meta ad properties for Android and iOS placement IDs.
 */
fun metaPlacementProperties(
    androidPlacementId: String,
    iosPlacementId: String,
    tag: String? = null
): MetaAdProperties {
    return MetaAdProperties(androidPlacementId, iosPlacementId, tag)
}
