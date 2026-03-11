package io.github.saifullah.nurani.ads.core

import androidx.compose.runtime.Stable

/**
 * iOS-specific implementation of the multiplatform AdLifecycleObserver.
 *
 * Unlike Android, iOS does not provide a universal lifecycle system like
 * LifecycleOwner. Therefore lifecycle events must be forwarded manually
 * from iOS components such as UIViewController or SwiftUI lifecycle
 * callbacks.
 *
 * These methods allow the iOS application layer to notify the shared
 * ad system when the UI becomes active, inactive, or destroyed.
 */
@Stable
actual interface AdLifecycleObserver {

    /**
     * Called when the iOS view or screen becomes active.
     *
     * Typical mapping in iOS:
     * - UIViewController.viewDidAppear()
     * - SwiftUI .onAppear()
     *
     * This event can be used to resume operations such as:
     * - ad refresh scheduling
     * - retry timers
     * - pending ad loads
     */
    actual fun onStart()

    /**
     * Called when the iOS view or screen is no longer visible.
     *
     * Typical mapping in iOS:
     * - UIViewController.viewWillDisappear()
     * - SwiftUI .onDisappear()
     *
     * This event is typically used to pause operations such as:
     * - scheduled ad refresh
     * - background tasks
     * - retry timers
     */
    actual fun onStop()

    /**
     * Called when the iOS component is permanently destroyed.
     *
     * Typical mapping in iOS:
     * - UIViewController.deinit
     * - SwiftUI view disposal
     *
     * Implementations should release resources and cancel any
     * scheduled operations to prevent memory leaks.
     */
    actual fun onDestroy()
}