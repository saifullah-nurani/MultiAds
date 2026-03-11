package io.github.saifullah.nurani.ads.core.utils

import android.util.Log
import io.github.saifullah.nurani.ads.core.AdLogger

actual class DefaultAdLogger actual constructor(tag: String?) : AdLogger {
    val tag: String = tag ?: "DefaultAdLogger"
    actual override fun d(message: String?) {
        if (message != null) {
            Log.d(tag, message)
        }
    }

    actual override fun e(message: String?) {
        if (message != null) {
            Log.e(tag, message)
        }
    }
}