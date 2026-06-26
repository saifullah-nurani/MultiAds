package io.github.saifullah.nurani.ads.ironsource.compose

import androidx.compose.runtime.Immutable

@Immutable
data class IronSourceAdProperties internal constructor(
    val androidPlacementName: String?,
    val iosPlacementName: String?,
    val tag: String? = null,
)

val IronSourceAdProperties.iosAdUnitId: String?
    get() = iosPlacementName

/**
 * Creates IronSource ad properties.
 *
 * Android uses the legacy placement name API. iOS uses the LevelPlay ad unit ID API.
 */
fun ironSourceAdProperties(
    androidPlacementName: String? = null,
    iosPlacementName: String? = null,
    iosAdUnitId: String? = null,
    tag: String? = null
): IronSourceAdProperties {
    return IronSourceAdProperties(androidPlacementName, iosAdUnitId ?: iosPlacementName, tag)
}
