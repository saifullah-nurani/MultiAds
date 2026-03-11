package io.github.saifullah.nurani.ads.core
import androidx.compose.runtime.Stable
import androidx.lifecycle.LifecycleOwner

/**
 * Defines lifecycle awareness for Ad units.
 *
 *
 * This interface allows an Ad implementation to observe and react.
 *
 *
 * Implementations typically use this to:
 *
 *  * Pause refresh timers when the UI is not visible
 *  * Resume loading or refreshing when the UI becomes active
 *  * Release resources to prevent memory leaks
 *
 *
 *
 * Usually connected to a [LifecycleOwner] such as:
 *
 *  * Activity
 *  * Fragment
 *
 */
@Stable
expect interface AdLifecycleObserver {

    /**
     * Called when the host lifecycle enters the ON_STOP state.
     *
     *
     * Typical use cases:
     *
     *  * Pause refresh tasks
     *  * Stop background timers
     *
     */
    fun onStop()

    /**
     * Called when the host lifecycle enters the ON_START state.
     *
     *
     * Typical use cases:
     *
     *  * Resume refresh operations
     *  * Trigger deferred ad loading
     *
     */
    fun onStart()

    /**
     * Called when the host lifecycle enters the ON_DESTROY state.
     *
     *
     * Implementations should:
     *
     *  * Release resources
     *  * Cancel pending retry or refresh tasks
     *  * Prevent memory leaks
     *
     */
    fun onDestroy()
}