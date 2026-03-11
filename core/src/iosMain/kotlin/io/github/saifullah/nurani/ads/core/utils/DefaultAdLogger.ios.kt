package io.github.saifullah.nurani.ads.core.utils

import io.github.saifullah.nurani.ads.core.AdLogger
import platform.Foundation.NSLog

actual class DefaultAdLogger actual constructor(tag: String?) : AdLogger {
    val tag: String = tag ?: "DefaultAdLogger"
    actual override fun d(message: String?) {
        if (message != null) {
            NSLog("$tag : $message")
        }
    }

    actual override fun e(message: String?) {
        if (message != null) {
            NSLog("$tag : $message")
        }
    }
}