@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package io.github.saifullah.nurani.ads.ironsource

import IronSource.LPMInitRequest
import IronSource.LPMInitRequestBuilder
import IronSource.LevelPlay
import io.github.saifullah.nurani.ads.core.AdInitResult

actual object IronSourceAds {
    const val TEST_APP_KEY: String = "2636d3a1d"

    private var isInitialized = false
    private var isInitializing = false
    private val pendingActions = mutableListOf<() -> Unit>()

    fun init(appKey: String, onComplete: ((Boolean) -> Unit)? = null) {
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
        val request: LPMInitRequest = LPMInitRequestBuilder(appKey).build()
        LevelPlay.initWithRequest(request) { config, error ->
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
        androidAppKey: String,
        iosAppKey: String,
        onComplete: ((AdInitResult) -> Unit)?
    ) {
        init(iosAppKey) { success ->
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
