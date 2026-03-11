package io.github.saifullah.nurani.ads.admob

import com.google.android.gms.ads.LoadAdError
import io.github.saifullah.nurani.ads.core.AdError

object AdmobUtils {
    @JvmStatic
    fun adErrorFrom(adError: LoadAdError): AdError {
        return AdError(adError.code, adError.message, adError, null)
    }

    @JvmStatic
    fun adErrorFrom(adError: com.google.android.gms.ads.AdError): AdError {
        return AdError(adError.code, adError.message, adError, null)
    }
}
