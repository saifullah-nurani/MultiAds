package io.github.saifullah.nurani.ads.applovin.compose

import androidx.compose.runtime.Immutable

@Immutable
data class AppLovinAdProperties internal constructor(
    val androidAdUnitId: String,
    val iosAdUnitId: String,
)

/**
 * Creates AppLovin ad properties for Android and iOS ad unit IDs.
 */
fun appLovinAdProperties(androidAdUnitId: String, iosAdUnitId: String): AppLovinAdProperties {
    return AppLovinAdProperties(androidAdUnitId, iosAdUnitId)
}
