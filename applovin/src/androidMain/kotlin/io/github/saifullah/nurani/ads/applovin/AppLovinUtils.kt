package io.github.saifullah.nurani.ads.applovin

import io.github.saifullah.nurani.ads.core.AdError
import com.applovin.mediation.MaxError

object AppLovinUtils {
    @JvmStatic
    fun adErrorFrom(error: MaxError): AdError {
        return AdError(error.code, error.message, null, null)
    }
}
