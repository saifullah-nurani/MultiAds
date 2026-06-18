package io.github.saifullah.nurani.ads.multi

import io.github.saifullah.nurani.ads.core.AppOpenAd
import io.github.saifullah.nurani.ads.core.AppOpenAdLifecycleManager
import io.github.saifullah.nurani.ads.core.compose.PlatformActivity
import androidx.lifecycle.Lifecycle

actual object PlatformAdLifecycleHelper {
    actual fun registerTestTrigger(activity: PlatformActivity?, ad: AppOpenAd, onLog: (String) -> Unit) {
        if (activity == null) return
        try {
            val app = activity.application
            AppOpenAdLifecycleManager.init(app, ad)
            
            // Clear existing first
            AppOpenAdLifecycleManager.clearRules()

            // Register rule: Trigger ON_START on this activity (or any activity)
            AppOpenAdLifecycleManager.registerActivityRule(
                activityClass = activity.javaClass,
                event = Lifecycle.Event.ON_START,
                condition = {
                    onLog("Auto-Trigger condition checked for activity: ON_START. Showing ad...")
                    true
                }
            )
            onLog("Auto-Trigger rule registered on ${activity.javaClass.simpleName} for ON_START event.")
        } catch (e: Exception) {
            onLog("Failed to initialize trigger manager: ${e.message}")
        }
    }

    actual fun clearTriggers() {
        try {
            AppOpenAdLifecycleManager.clearRules()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
