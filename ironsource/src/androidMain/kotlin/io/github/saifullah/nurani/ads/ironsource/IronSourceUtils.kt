package io.github.saifullah.nurani.ads.ironsource

import com.ironsource.mediationsdk.logger.IronSourceError
import io.github.saifullah.nurani.ads.core.AdError

object IronSourceUtils {
    @JvmStatic
    fun adErrorFrom(error: IronSourceError): AdError {
        return AdError(error.errorCode, error.errorMessage ?: "", null, null)
    }
}
