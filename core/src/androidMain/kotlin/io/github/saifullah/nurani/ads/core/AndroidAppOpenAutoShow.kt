package io.github.saifullah.nurani.ads.core

import android.app.Activity
import android.app.Application
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import kotlin.reflect.KClass

class AndroidAppOpenAutoShowBuilder internal constructor() {
    internal val activityRules = mutableListOf<ActivityRule>()
    internal val fragmentRules = mutableListOf<FragmentRule>()

    fun anyActivity(
        event: Lifecycle.Event = Lifecycle.Event.ON_RESUME,
        condition: (Activity) -> Boolean = { true }
    ) {
        activity(Activity::class, event, condition)
    }

    fun activity(
        activityClass: KClass<out Activity>,
        event: Lifecycle.Event = Lifecycle.Event.ON_RESUME,
        condition: (Activity) -> Boolean = { true }
    ) {
        activityRules += ActivityRule(activityClass, event, condition)
    }

    inline fun <reified T : Activity> activity(
        event: Lifecycle.Event = Lifecycle.Event.ON_RESUME,
        noinline condition: (T) -> Boolean = { true }
    ) {
        activity(T::class, event) { activity -> condition(activity as T) }
    }

    fun fragment(
        fragmentClass: KClass<out Fragment>,
        event: Lifecycle.Event = Lifecycle.Event.ON_RESUME,
        condition: (Fragment) -> Boolean = { true }
    ) {
        fragmentRules += FragmentRule(fragmentClass, event, condition)
    }

    inline fun <reified T : Fragment> fragment(
        event: Lifecycle.Event = Lifecycle.Event.ON_RESUME,
        noinline condition: (T) -> Boolean = { true }
    ) {
        fragment(T::class, event) { fragment -> condition(fragment as T) }
    }

    internal data class ActivityRule(
        val activityClass: KClass<out Activity>,
        val event: Lifecycle.Event,
        val condition: (Activity) -> Boolean
    )

    internal data class FragmentRule(
        val fragmentClass: KClass<out Fragment>,
        val event: Lifecycle.Event,
        val condition: (Fragment) -> Boolean
    )
}

fun <T : AppOpenAd> T.bindToAndroidAppOpenAutoShow(
    application: Application,
    configure: AndroidAppOpenAutoShowBuilder.() -> Unit = {
        anyActivity(Lifecycle.Event.ON_RESUME)
    }
): T {
    val builder = AndroidAppOpenAutoShowBuilder().apply(configure)

    AppOpenAdLifecycleManager.init(application, this)
    AppOpenAdLifecycleManager.setAd(this)
    AppOpenAdLifecycleManager.clearRules()

    if (builder.activityRules.isEmpty() && builder.fragmentRules.isEmpty()) {
        AppOpenAdLifecycleManager.registerActivityRule(Activity::class.java, Lifecycle.Event.ON_RESUME)
        return this
    }

    builder.activityRules.forEach { rule ->
        AppOpenAdLifecycleManager.registerActivityRule(
            activityClass = rule.activityClass.java,
            event = rule.event,
            condition = rule.condition
        )
    }
    builder.fragmentRules.forEach { rule ->
        AppOpenAdLifecycleManager.registerFragmentRule(
            fragmentClass = rule.fragmentClass.java,
            event = rule.event,
            condition = rule.condition
        )
    }
    return this
}
