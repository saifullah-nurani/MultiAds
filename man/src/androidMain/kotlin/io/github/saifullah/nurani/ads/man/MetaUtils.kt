package io.github.saifullah.nurani.ads.man

import io.github.saifullah.nurani.ads.core.AdError

object MetaUtils {
    @JvmStatic
    fun adErrorFrom(error: com.facebook.ads.AdError): AdError {
        return AdError(error.errorCode, error.errorMessage, null, null)
    }
}
