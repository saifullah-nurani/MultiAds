package io.github.saifullah.nurani.ads.multi

import io.github.saifullah.nurani.ads.core.AppOpenAd
import io.github.saifullah.nurani.ads.core.compose.PlatformActivity

actual object PlatformAdLifecycleHelper {
    actual fun registerTestTrigger(activity: PlatformActivity?, ad: AppOpenAd, onLog: (String) -> Unit) {
        onLog("Auto-Trigger lifecycle observer not supported on iOS (Android only).")
    }

    actual fun clearTriggers() {}
}
