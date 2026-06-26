@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package io.github.saifullah.nurani.ads.inmobi

import InMobiSDK.IMSdk
import io.github.saifullah.nurani.ads.core.AdInitResult

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
        androidAccountId: String,
        iosAccountId: String,
        onComplete: ((AdInitResult) -> Unit)?
    ) {
        init(iosAccountId) { success ->
            onComplete?.invoke(AdInitResult(success))
        }
    }

    actual fun isInitialized(): Boolean = isInitialized
}
