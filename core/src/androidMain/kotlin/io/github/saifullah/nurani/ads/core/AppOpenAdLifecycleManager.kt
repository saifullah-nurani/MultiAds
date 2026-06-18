package io.github.saifullah.nurani.ads.core

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner

/**
 * A manager that observes Android Activity and Fragment lifecycle events
 * and automatically triggers [AppOpenAd] display based on customizable matching rules.
 */
object AppOpenAdLifecycleManager {
    private var isInitialized = false
    private val activityRules = mutableListOf<ActivityTriggerRule>()
    private val fragmentRules = mutableListOf<FragmentTriggerRule>()
    private var appOpenAd: AppOpenAd? = null

    class ActivityTriggerRule(
        val activityClass: Class<out Activity>,
        val event: Lifecycle.Event,
        val condition: (Activity) -> Boolean
    )

    class FragmentTriggerRule(
        val fragmentClass: Class<out Fragment>,
        val event: Lifecycle.Event,
        val condition: (Fragment) -> Boolean
    )

    /**
     * Initializes the manager with the application context and the App Open Ad instance.
     */
    fun init(application: Application, ad: AppOpenAd) {
        if (isInitialized) {
            appOpenAd = ad
            return
        }
        isInitialized = true
        appOpenAd = ad

        application.registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                // Monitor activity lifecycle
                (activity as? LifecycleOwner)?.lifecycle?.addObserver(LifecycleEventObserver { _, event ->
                    handleActivityLifecycleEvent(activity, event)
                })

                // Monitor fragments if it is a FragmentActivity (androidx)
                (activity as? FragmentActivity)?.supportFragmentManager?.registerFragmentLifecycleCallbacks(
                    object : FragmentManager.FragmentLifecycleCallbacks() {
                        override fun onFragmentCreated(fm: FragmentManager, f: Fragment, savedInstanceState: Bundle?) {
                            f.lifecycle.addObserver(object : LifecycleEventObserver {
                                override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                                    handleFragmentLifecycleEvent(f, event)
                                    if (event == Lifecycle.Event.ON_DESTROY) {
                                        f.lifecycle.removeObserver(this)
                                    }
                                }
                            })
                        }
                    },
                    true // recursive, so we get nested fragments too
                )
            }

            override fun onActivityStarted(activity: Activity) {}
            override fun onActivityResumed(activity: Activity) {}
            override fun onActivityPaused(activity: Activity) {}
            override fun onActivityStopped(activity: Activity) {}
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
            override fun onActivityDestroyed(activity: Activity) {}
        })
    }

    /**
     * Sets or updates the App Open Ad instance.
     */
    fun setAd(ad: AppOpenAd) {
        appOpenAd = ad
    }

    /**
     * Registers a custom rule to show the ad when a specific Activity lifecycle event is triggered.
     */
    fun registerActivityRule(
        activityClass: Class<out Activity>,
        event: Lifecycle.Event = Lifecycle.Event.ON_START,
        condition: (Activity) -> Boolean = { true }
    ) {
        activityRules.add(ActivityTriggerRule(activityClass, event, condition))
    }

    /**
     * Registers a custom rule to show the ad when a specific Fragment lifecycle event is triggered.
     */
    fun registerFragmentRule(
        fragmentClass: Class<out Fragment>,
        event: Lifecycle.Event = Lifecycle.Event.ON_START,
        condition: (Fragment) -> Boolean = { true }
    ) {
        fragmentRules.add(FragmentTriggerRule(fragmentClass, event, condition))
    }

    /**
     * Clears all registered rules.
     */
    fun clearRules() {
        activityRules.clear()
        fragmentRules.clear()
    }

    private fun handleActivityLifecycleEvent(activity: Activity, event: Lifecycle.Event) {
        val ad = appOpenAd ?: return
        val matches = activityRules.any { rule ->
            rule.activityClass.isInstance(activity) && rule.event == event && rule.condition(activity)
        }
        if (matches) {
            if (ad.isAdAvailable) {
                ad.showAd(activity)
            } else {
                ad.loadAd()
            }
        }
    }

    private fun handleFragmentLifecycleEvent(fragment: Fragment, event: Lifecycle.Event) {
        val ad = appOpenAd ?: return
        val matches = fragmentRules.any { rule ->
            rule.fragmentClass.isInstance(fragment) && rule.event == event && rule.condition(fragment)
        }
        if (matches) {
            val activity = fragment.activity
            if (activity != null) {
                if (ad.isAdAvailable) {
                    ad.showAd(activity)
                } else {
                    ad.loadAd()
                }
            }
        }
    }
}
