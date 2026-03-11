package io.github.saifullah.nurani.ads.core

import androidx.compose.runtime.Stable

/**
 * Platform-independent scheduler used to execute delayed tasks in a Kotlin Multiplatform project.
 *
 * This abstraction replaces platform-specific scheduling mechanisms such as:
 * - Android: Handler.postDelayed()
 * - iOS: dispatch_after()
 *
 * The actual implementation is provided in platform-specific source sets
 * (`androidMain`, `iosMain`, etc.) using `actual class Scheduler`.
 *
 * The purpose of this class is to allow shared business logic (e.g., ad refresh,
 * retry mechanisms, timed operations) to schedule tasks without depending
 * on platform-specific APIs.
 */
@Stable
expect class Scheduler() {

    /**
     * Schedules a task to run after a specified delay.
     *
     * @param delayMillis The delay duration in milliseconds before the task executes.
     * @param task The function to execute after the delay.
     *
     * Example usage:
     * ```
     * scheduler.schedule(5000) {
     *     loadAd()
     * }
     * ```
     */
    fun schedule(delayMillis: Long, task: () -> Unit)

    /**
     * Cancels a previously scheduled task.
     *
     * If the task has not yet executed, it will be removed from the queue
     * and will not run.
     *
     * @param task The same task reference that was previously scheduled.
     */
    fun cancel(task: () -> Unit)
}