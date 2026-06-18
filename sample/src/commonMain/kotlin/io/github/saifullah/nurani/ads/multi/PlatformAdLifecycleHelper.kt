package io.github.saifullah.nurani.ads.multi

import io.github.saifullah.nurani.ads.core.AppOpenAd
import io.github.saifullah.nurani.ads.core.compose.PlatformActivity

expect object PlatformAdLifecycleHelper {
    fun registerTestTrigger(activity: PlatformActivity?, ad: AppOpenAd, onLog: (String) -> Unit)
    fun clearTriggers()
}
