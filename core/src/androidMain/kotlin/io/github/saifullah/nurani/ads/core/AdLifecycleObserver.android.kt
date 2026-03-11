package io.github.saifullah.nurani.ads.core

import androidx.compose.runtime.Stable
import androidx.lifecycle.LifecycleOwner


/**
 * Android-specific lifecycle observer used to bridge Android's
 * LifecycleOwner events to the shared Ad lifecycle system.
 *
 * This interface allows the shared KMM ad components to react
 * to Android lifecycle events such as start, stop, and destroy
 * without directly depending on Android APIs in common code.
 *
 * Implementations typically register themselves with a
 * [LifecycleOwner] and forward lifecycle callbacks to
 * the shared ad state manager.
 */
@Stable
actual interface AdLifecycleObserver {

    /**
     * Attaches this observer to a given [LifecycleOwner].
     *
     * Implementations should register themselves to receive
     * lifecycle callbacks from the provided owner.
     *
     * Example:
     * ```
     * lifecycleOwner.lifecycle.addObserver(...)
     * ```
     *
     * @param owner The Android lifecycle owner (Activity, Fragment, etc.)
     *              whose lifecycle events should be observed.
     */
    fun addLifecycleOwner(owner: LifecycleOwner)

    /**
     * Called when the lifecycle owner enters the **STOPPED** state.
     *
     * This is typically used to pause operations such as:
     * - ad refreshing
     * - timers
     * - scheduled tasks
     */
    actual fun onStop()

    /**
     * Called when the lifecycle owner enters the **STARTED** state.
     *
     * This is commonly used to resume operations such as:
     * - ad refreshing
     * - retry mechanisms
     * - scheduled ad loading
     */
    actual fun onStart()

    /**
     * Called when the lifecycle owner is **destroyed**.
     *
     * Implementations should release resources, cancel scheduled
     * tasks, and perform any necessary cleanup to avoid memory leaks.
     */
    actual fun onDestroy()
}