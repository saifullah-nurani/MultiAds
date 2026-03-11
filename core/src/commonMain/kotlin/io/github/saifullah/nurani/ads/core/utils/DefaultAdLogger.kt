package io.github.saifullah.nurani.ads.core.utils

import io.github.saifullah.nurani.ads.core.AdLogger

expect class DefaultAdLogger(tag: String?=null): AdLogger {
    override fun d(message: String?)
    override fun e(message: String?)
}
