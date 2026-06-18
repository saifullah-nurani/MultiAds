package io.github.saifullah.nurani.ads.admob.compose

import androidx.compose.runtime.Immutable

@Immutable
data class AdmobAdProperties internal constructor(
    val androidAdUnitId: String,
    val iosAdUnitId: String,
)

fun admobAdProperties(androidAdUnitId: String, iosAdUnitId: String): AdmobAdProperties {
    return AdmobAdProperties(androidAdUnitId, iosAdUnitId)
}
