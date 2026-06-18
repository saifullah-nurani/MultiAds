package io.github.saifullah.nurani.ads.vungle

import io.github.saifullah.nurani.ads.core.AdError

object VungleUtils {
    @JvmStatic
    fun adErrorFrom(error: com.vungle.ads.VungleError): AdError {
        return AdError(error.code, error.errorMessage ?: "", null, null)
    }
}
