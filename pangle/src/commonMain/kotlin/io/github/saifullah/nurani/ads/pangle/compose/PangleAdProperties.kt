package io.github.saifullah.nurani.ads.pangle.compose

import androidx.compose.runtime.Immutable

@Immutable
data class PangleAdProperties internal constructor(
    val androidAdUnitId: String,
    val iosAdUnitId: String,
    val tag: String? = null,
)

/**
 * Creates Pangle ad properties for Android and iOS ad unit IDs.
 */
fun pangleAdProperties(
    androidAdUnitId: String,
    iosAdUnitId: String,
    tag: String? = null
): PangleAdProperties {
    return PangleAdProperties(androidAdUnitId, iosAdUnitId, tag)
}
