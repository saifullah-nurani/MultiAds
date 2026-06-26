package io.github.saifullah.nurani.ads.core

data class AdInitResult(
    val success: Boolean,
    val error: AdError? = null
)
