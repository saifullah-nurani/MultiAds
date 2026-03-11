package io.github.saifullah.nurani.ads.core
import androidx.compose.runtime.Stable
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import platform.darwin.*
import kotlin.collections.MutableMap
import kotlin.collections.mutableMapOf

/**
 * iOS-specific implementation of the multiplatform Scheduler.
 *
 * This implementation schedules delayed tasks using
 * Grand Central Dispatch (GCD) and DispatchWorkItem.
 *
 * DispatchWorkItem allows tasks to be cancelled before execution,
 * making it suitable for retry systems, refresh schedulers,
 * and other delayed operations.
 */
@Stable
actual class Scheduler {

    /**
     * Maps Kotlin tasks to DispatchWorkItems
     * so scheduled tasks can be cancelled safely.
     */
    private data class WorkItem(
        val block: dispatch_block_t,
        var cancelled: Boolean = false
    )

    private val workItemMap = mutableMapOf<() -> Unit, WorkItem>()

    /**
     * Schedules a task to run after a specified delay.
     *
     * @param delayMillis Delay duration in milliseconds.
     * @param task The task to execute.
     */
    actual fun schedule(delayMillis: Long, task: () -> Unit) {
        val item = WorkItem(
            block = dispatch_block_create(0u) {
                val entry = workItemMap[task]

                if (entry?.cancelled == true) return@dispatch_block_create

                task()

                workItemMap.remove(task)
            }
        )

        workItemMap[task] = item

        dispatch_after(
            dispatch_time(DISPATCH_TIME_NOW, delayMillis * 1_000_000),
            dispatch_get_main_queue(),
            item.block
        )
    }

    /**
     * Cancels a previously scheduled task if it has not executed yet.
     *
     * @param task The same task reference used during scheduling.
     */
    actual fun cancel(task: () -> Unit) {
        val item = workItemMap[task] ?: return
        item.cancelled = true
        workItemMap.remove(task)
    }
}