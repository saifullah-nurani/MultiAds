package io.github.saifullah.nurani.ads.core

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner

/**
 * Android-specific lifecycle manager that bridges Android Lifecycle
 * events to the shared KMM lifecycle callbacks.
 *
 * This class registers itself as a [DefaultLifecycleObserver] to
 * receive lifecycle events from a [LifecycleOwner] such as an
 * Activity or Fragment.
 *
 * Subclasses can override [onStart], [onStop], and [onDestroy]
 * to react to lifecycle changes.
 */
actual open class DefaultAdLifecycleManager : DefaultLifecycleObserver, AdLifecycleObserver {

    /**
     * The lifecycle associated with the current owner.
     */
    private var lifecycle: Lifecycle? = null

    /**
     * Attaches this lifecycle manager to a [LifecycleOwner].
     *
     * This registers the manager as a lifecycle observer so
     * lifecycle callbacks can be received automatically.
     *
     * @param owner Activity or Fragment lifecycle owner.
     */
    override fun addLifecycleOwner(owner: LifecycleOwner) {
        lifecycle = owner.lifecycle
        lifecycle?.addObserver(this)
    }

    /**
     * Called when the lifecycle enters the STOPPED state.
     *
     * Subclasses may override this to pause operations
     * such as timers, refresh scheduling, or background work.
     */
    actual override fun onStop() {
        // intended for subclasses
    }

    /**
     * Called when the lifecycle enters the STARTED state.
     *
     * Subclasses may override this to resume operations
     * such as ad refresh scheduling or pending tasks.
     */
    actual override fun onStart() {
        // intended for subclasses
    }

    /**
     * Called when the lifecycle is destroyed.
     *
     * This removes the lifecycle observer to prevent
     * memory leaks.
     */
    actual override fun onDestroy() {
        lifecycle?.removeObserver(this)
        lifecycle = null
    }

    /**
     * Android lifecycle callback forwarded to shared lifecycle method.
     */
    override fun onStart(owner: LifecycleOwner) {
        onStart()
    }

    /**
     * Android lifecycle callback forwarded to shared lifecycle method.
     */
    override fun onStop(owner: LifecycleOwner) {
        onStop()
    }

    /**
     * Android lifecycle callback forwarded to shared lifecycle method.
     */
    override fun onDestroy(owner: LifecycleOwner) {
        onDestroy()
    }
}