package io.github.saifullah.nurani.ads.admob.compose

import androidx.compose.runtime.Immutable

@Immutable
data class AdmobAdProperties internal constructor(
    val androidAdUnitId: String,
    val iosAdUnitId: String,
    val tag: String? = null,
)

fun admobAdProperties(
    androidAdUnitId: String,
    iosAdUnitId: String,
    tag: String? = null
): AdmobAdProperties {
    return AdmobAdProperties(androidAdUnitId, iosAdUnitId, tag)
}
