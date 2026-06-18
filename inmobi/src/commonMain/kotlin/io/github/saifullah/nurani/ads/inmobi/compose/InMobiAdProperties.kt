package io.github.saifullah.nurani.ads.inmobi.compose

import androidx.compose.runtime.Immutable

@Immutable
data class InMobiAdProperties internal constructor(
    val androidPlacementId: Long,
    val iosPlacementId: Long,
)

/**
 * Creates InMobi ad properties for Android and iOS placement IDs.
 */
fun inMobiPlacementProperties(androidPlacementId: Long, iosPlacementId: Long): InMobiAdProperties {
    return InMobiAdProperties(androidPlacementId, iosPlacementId)
}
