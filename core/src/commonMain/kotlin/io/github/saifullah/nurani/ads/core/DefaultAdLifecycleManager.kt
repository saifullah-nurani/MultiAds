package io.github.saifullah.nurani.ads.core

/**
 * Platform-independent lifecycle manager used by the ad system
 * to receive lifecycle events from platform-specific UI layers.
 *
 * This class defines lifecycle callbacks that must be implemented
 * in platform-specific modules (Android and iOS) using `actual`
 * implementations.
 *
 * Responsibilities:
 * - Receive lifecycle events from the platform
 * - Forward those events to shared ad logic
 * - Allow subclasses to react to lifecycle changes
 *
 * Platform implementations:
 *
 * Android:
 * - Typically integrates with `LifecycleOwner`
 * - Uses `DefaultLifecycleObserver`
 *
 * iOS:
 * - Lifecycle events are forwarded manually from
 *   `UIViewController` or SwiftUI views
 */
expect open class DefaultAdLifecycleManager() : AdLifecycleObserver {

    /**
     * Called when the associated UI component becomes active.
     *
     * Typical usage:
     * - Resume scheduled tasks
     * - Resume ad refresh operations
     * - Trigger pending ad loads
     */
    override fun onStart()

    /**
     * Called when the associated UI component is no longer visible.
     *
     * Typical usage:
     * - Pause refresh scheduling
     * - Stop background tasks
     * - Suspend retry mechanisms
     */
    override fun onStop()

    /**
     * Called when the UI component is permanently destroyed.
     *
     * Typical usage:
     * - Cancel scheduled operations
     * - Release resources
     * - Prevent memory leaks
     */
    override fun onDestroy()
}