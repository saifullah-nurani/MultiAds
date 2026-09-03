package io.github.saifullah.nurani.ads.ironsource.compose

import androidx.compose.runtime.Immutable

@Immutable
data class IronSourceAdProperties internal constructor(
    val androidAdUnitId: String?,
    val iosAdUnitId: String?,
    val tag: String? = null,
)

val IronSourceAdProperties.adUnitId: String?
    get() = androidAdUnitId ?: iosAdUnitId

/**
 * Creates IronSource ad properties for LevelPlay Ad Unit APIs.
 */
fun ironSourceAdProperties(
    androidAdUnitId: String? = null,
    iosAdUnitId: String? = null,
    tag: String? = null,
): IronSourceAdProperties {
    return IronSourceAdProperties(androidAdUnitId, iosAdUnitId, tag)
}
