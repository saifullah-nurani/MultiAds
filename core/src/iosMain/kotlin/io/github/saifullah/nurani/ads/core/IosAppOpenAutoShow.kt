@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package io.github.saifullah.nurani.ads.core

import platform.Foundation.NSNotificationCenter
import platform.UIKit.UIApplicationDidBecomeActiveNotification
import platform.UIKit.UIApplicationWillEnterForegroundNotification
import platform.UIKit.UIViewController
import kotlin.reflect.KClass

enum class IosAppOpenLifecycleState {
    DID_BECOME_ACTIVE,
    WILL_ENTER_FOREGROUND,
    VIEW_WILL_APPEAR,
    VIEW_DID_APPEAR
}

class IosAppOpenAutoShowBuilder internal constructor() {
    internal val appStates = mutableSetOf<IosAppOpenLifecycleState>()
    internal val viewControllerRules = mutableListOf<ViewControllerRule>()

    fun appDidBecomeActive() {
        appStates += IosAppOpenLifecycleState.DID_BECOME_ACTIVE
    }

    fun appWillEnterForeground() {
        appStates += IosAppOpenLifecycleState.WILL_ENTER_FOREGROUND
    }

    fun viewController(
        controllerClass: KClass<out UIViewController>,
        state: IosAppOpenLifecycleState = IosAppOpenLifecycleState.VIEW_DID_APPEAR,
        condition: (UIViewController) -> Boolean = { true }
    ) {
        viewControllerRules += ViewControllerRule(controllerClass, state, condition)
    }

    inline fun <reified T : UIViewController> viewController(
        state: IosAppOpenLifecycleState = IosAppOpenLifecycleState.VIEW_DID_APPEAR,
        noinline condition: (T) -> Boolean = { true }
    ) {
        viewController(T::class, state) { controller -> condition(controller as T) }
    }

    internal data class ViewControllerRule(
        val controllerClass: KClass<out UIViewController>,
        val state: IosAppOpenLifecycleState,
        val condition: (UIViewController) -> Boolean
    )
}

class IosAppOpenAutoShowHandle internal constructor(
    private val ad: AppOpenAd,
    private val appStates: Set<IosAppOpenLifecycleState>,
    private val viewControllerRules: List<IosAppOpenAutoShowBuilder.ViewControllerRule>,
    private val observerTokens: List<Any>
) {
    fun notify(state: IosAppOpenLifecycleState, viewController: UIViewController? = null) {
        when (state) {
            IosAppOpenLifecycleState.DID_BECOME_ACTIVE,
            IosAppOpenLifecycleState.WILL_ENTER_FOREGROUND -> {
                if (state in appStates) {
                    showOrLoad(viewController ?: currentRootViewController())
                }
            }

            IosAppOpenLifecycleState.VIEW_WILL_APPEAR,
            IosAppOpenLifecycleState.VIEW_DID_APPEAR -> {
                val controller = viewController ?: return
                val matched = viewControllerRules.any { rule ->
                    rule.state == state &&
                        rule.controllerClass.isInstance(controller) &&
                        rule.condition(controller)
                }
                if (matched) {
                    showOrLoad(controller)
                }
            }
        }
    }

    fun notifyViewWillAppear(viewController: UIViewController) {
        notify(IosAppOpenLifecycleState.VIEW_WILL_APPEAR, viewController)
    }

    fun notifyViewDidAppear(viewController: UIViewController) {
        notify(IosAppOpenLifecycleState.VIEW_DID_APPEAR, viewController)
    }

    fun dispose() {
        observerTokens.forEach { token ->
            NSNotificationCenter.defaultCenter.removeObserver(token)
        }
    }

    private fun showOrLoad(viewController: UIViewController?) {
        if (ad.isAdAvailable && viewController != null) {
            ad.showAd(viewController)
        } else {
            ad.loadAd()
        }
    }
}

fun <T : AppOpenAd> T.bindToIosAppOpenAutoShow(
    configure: IosAppOpenAutoShowBuilder.() -> Unit = {
        appDidBecomeActive()
    }
): IosAppOpenAutoShowHandle {
    val builder = IosAppOpenAutoShowBuilder().apply(configure)
    val center = NSNotificationCenter.defaultCenter
    val tokens = mutableListOf<Any>()

    val handle = IosAppOpenAutoShowHandle(
        ad = this,
        appStates = builder.appStates.toSet(),
        viewControllerRules = builder.viewControllerRules.toList(),
        observerTokens = tokens
    )

    if (IosAppOpenLifecycleState.DID_BECOME_ACTIVE in builder.appStates) {
        tokens += center.addObserverForName(
            name = UIApplicationDidBecomeActiveNotification,
            `object` = null,
            queue = null
        ) { handle.notify(IosAppOpenLifecycleState.DID_BECOME_ACTIVE) }
    }

    if (IosAppOpenLifecycleState.WILL_ENTER_FOREGROUND in builder.appStates) {
        tokens += center.addObserverForName(
            name = UIApplicationWillEnterForegroundNotification,
            `object` = null,
            queue = null
        ) { handle.notify(IosAppOpenLifecycleState.WILL_ENTER_FOREGROUND) }
    }

    return handle
}

private fun currentRootViewController(): UIViewController? {
    return platform.UIKit.UIApplication.sharedApplication.keyWindow?.rootViewController
}
