@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package io.github.saifullah.nurani.ads.inmobi

import InMobiSDK.IMSdk
import io.github.saifullah.nurani.ads.core.AdInitResult

actual object InMobiAds {
    private var isInitialized = false
    private var isInitializing = false
    private val pendingActions = mutableListOf<() -> Unit>()

    fun init(accountId: String, onComplete: ((Boolean) -> Unit)? = null) {
        if (isInitialized) {
            onComplete?.invoke(true)
            flushPendingActions()
            return
        }
        if (isInitializing) {
            onComplete?.let { callback ->
                pendingActions += { callback(isInitialized) }
            }
            return
        }
        isInitializing = true
        IMSdk.initWithAccountID(accountId) { error ->
            isInitializing = false
            if (error == null) {
                isInitialized = true
                onComplete?.invoke(true)
                flushPendingActions()
            } else {
                onComplete?.invoke(false)
            }
        }
    }

    actual fun init(
        context: io.github.saifullah.nurani.ads.core.compose.PlatformContext,
        androidAccountId: String,
        iosAccountId: String,
        onComplete: ((AdInitResult) -> Unit)?
    ) {
        init(iosAccountId) { success ->
            onComplete?.invoke(AdInitResult(success))
        }
    }

    actual fun isInitialized(): Boolean = isInitialized

    internal fun runWhenInitialized(action: () -> Unit) {
        if (isInitialized) {
            action()
        } else {
            pendingActions += action
        }
    }

    private fun flushPendingActions() {
        val actions = pendingActions.toList()
        pendingActions.clear()
        actions.forEach { it() }
    }
}
