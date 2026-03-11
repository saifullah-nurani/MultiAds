package io.github.saifullah.nurani.ads.core.compose

import platform.UIKit.UIApplication

@Suppress("UNCHECKED_CAST")
actual inline fun <reified T, R> findOwner(context: T): R? =
    UIApplication.sharedApplication.keyWindow?.rootViewController as R?