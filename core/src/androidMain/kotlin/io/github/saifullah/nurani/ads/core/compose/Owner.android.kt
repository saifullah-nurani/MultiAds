package io.github.saifullah.nurani.ads.core.compose

import android.app.Activity
import android.content.ContextWrapper
actual inline fun <reified T, R> findOwner(context: T): R? {
    var innerContext = context
    while (innerContext is ContextWrapper) {
        if (innerContext is Activity) {
            @Suppress("UNCHECKED_CAST")
            return innerContext as? R?
        }
        innerContext = innerContext.baseContext as T
    }
    return null
}