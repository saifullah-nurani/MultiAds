package io.github.saifullah.nurani.ads.inmobi

import io.github.saifullah.nurani.ads.core.AdError
import com.inmobi.ads.InMobiAdRequestStatus

object InMobiUtils {
    @JvmStatic
    fun adErrorFrom(status: InMobiAdRequestStatus): AdError {
        return AdError(status.statusCode?.ordinal ?: -1, status.message ?: "", null, null)
    }
}
