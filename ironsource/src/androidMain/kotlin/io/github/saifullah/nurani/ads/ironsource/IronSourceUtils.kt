package io.github.saifullah.nurani.ads.ironsource

import com.ironsource.mediationsdk.logger.IronSourceError
import com.unity3d.mediation.LevelPlayAdError
import com.unity3d.mediation.LevelPlayInitError
import io.github.saifullah.nurani.ads.core.AdError

object IronSourceUtils {
    @JvmStatic
    fun adErrorFrom(error: LevelPlayAdError): AdError {
        return AdError(error.errorCode, error.errorMessage, null, null)
    }

    @JvmStatic
    fun adErrorFrom(error: LevelPlayInitError): AdError {
        return AdError(error.errorCode, error.errorMessage, null, null)
    }

    @JvmStatic
    fun adErrorFrom(error: IronSourceError): AdError {
        return AdError(error.errorCode, error.errorMessage ?: "", null, null)
    }
}

