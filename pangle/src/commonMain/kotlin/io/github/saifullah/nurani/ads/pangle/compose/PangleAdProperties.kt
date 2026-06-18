package io.github.saifullah.nurani.ads.pangle.compose

import androidx.compose.runtime.Immutable

@Immutable
data class PangleAdProperties internal constructor(
    val androidAdUnitId: String,
    val iosAdUnitId: String,
)

/**
 * Creates Pangle ad properties for Android and iOS ad unit IDs.
 */
fun pangleAdProperties(androidAdUnitId: String, iosAdUnitId: String): PangleAdProperties {
    return PangleAdProperties(androidAdUnitId, iosAdUnitId)
}
