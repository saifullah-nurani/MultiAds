@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package io.github.saifullah.nurani.ads.inmobi

import InMobiSDK.IMSdk

actual object InMobiAds {
    private var isInitialized = false

    fun init(accountId: String, onComplete: ((Boolean) -> Unit)? = null) {
        IMSdk.initWithAccountID(accountId) { error ->
            if (error == null) {
                isInitialized = true
                onComplete?.invoke(true)
            } else {
                onComplete?.invoke(false)
            }
        }
    }

    actual fun init(
        context: io.github.saifullah.nurani.ads.core.compose.PlatformContext,
        accountId: String,
        onComplete: ((Boolean) -> Unit)?
    ) {
        init(accountId, onComplete)
    }

    actual fun isInitialized(): Boolean = isInitialized
}
