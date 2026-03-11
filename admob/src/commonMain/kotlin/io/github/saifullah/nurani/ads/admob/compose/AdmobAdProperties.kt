package io.github.saifullah.nurani.ads.admob.compose

import androidx.compose.runtime.Immutable

@Immutable
class AdmobAdProperties(
    val androidAdUnitId: String,
    val iosAdUnitId: String,
){
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as AdmobAdProperties

        if (androidAdUnitId != other.androidAdUnitId) return false
        if (iosAdUnitId != other.iosAdUnitId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = androidAdUnitId.hashCode()
        result = 31 * result + iosAdUnitId.hashCode()
        return result
    }
}