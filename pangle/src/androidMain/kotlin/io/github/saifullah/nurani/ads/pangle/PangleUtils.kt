package io.github.saifullah.nurani.ads.pangle

import io.github.saifullah.nurani.ads.core.AdError

object PangleUtils {
    @JvmStatic
    fun adErrorFrom(code: Int, message: String): AdError {
        return AdError(code, message, null, null)
    }
}
