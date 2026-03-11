package io.github.saifullah.nurani.ads.core

import androidx.compose.runtime.Stable

/**
 * iOS-specific implementation of the AdLifecycleManager.
 *
 * Unlike Android, iOS does not provide a built-in lifecycle observer
 * system like AndroidX Lifecycle. Therefore lifecycle events must
 * be forwarded manually from iOS UI components such as:
 *
 * - UIViewController lifecycle methods
 * - SwiftUI view lifecycle callbacks
 *
 * Subclasses can override the lifecycle methods to react to
 * visibility or destruction of the screen and control ad
 * loading, refreshing, or cleanup logic.
 */
@Stable
actual open class DefaultAdLifecycleManager : AdLifecycleObserver {

    /**
     * Called when the associated iOS screen becomes active or visible.
     *
     * Typical mapping:
     * - UIViewController.viewDidAppear()
     * - SwiftUI .onAppear()
     *
     * Subclasses can override this to resume operations such as
     * ad refreshing or scheduled tasks.
     */
    actual override fun onStart() {
    }

    /**
     * Called when the associated iOS screen is no longer visible.
     *
     * Typical mapping:
     * - UIViewController.viewWillDisappear()
     * - SwiftUI .onDisappear()
     *
     * Subclasses can override this to pause operations such as
     * ad refresh scheduling or background tasks.
     */
    actual override fun onStop() {
    }

    /**
     * Called when the associated iOS component is destroyed.
     *
     * Typical mapping:
     * - UIViewController.deinit
     * - SwiftUI view disposal
     *
     * Subclasses can override this to release resources,
     * cancel scheduled operations, and perform cleanup.
     */
    actual override fun onDestroy() {
    }
}