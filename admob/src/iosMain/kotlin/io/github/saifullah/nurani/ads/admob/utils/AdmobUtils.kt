package io.github.saifullah.nurani.ads.admob.utils

import io.github.saifullah.nurani.ads.core.AdError
import platform.Foundation.NSError

fun NSError.adErrorFrom(): AdError {
    return AdError(code.toInt(), localizedDescription, this, null)
}

fun Exception.adErrorFrom(): AdError {
    return AdError(-1,message, this, null)
}
