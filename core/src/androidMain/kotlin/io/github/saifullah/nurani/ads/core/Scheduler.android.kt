package io.github.saifullah.nurani.ads.core
import androidx.compose.runtime.Stable
import android.os.Handler
import android.os.Looper
import java.util.concurrent.ConcurrentHashMap

/**
 * This scheduler executes delayed tasks using Android's [Handler].
 *
 * A custom [Handler] can optionally be provided to control which
 * thread executes scheduled tasks. If no handler is provided,
 * the scheduler defaults to the Android main thread.
 *
 * Example usages:
 *
 * Default main thread scheduler:
 * ```
 * val scheduler = Scheduler()
 * ```
 *
 * Custom handler scheduler:
 * ```
 * val handler = Handler(Looper.getMainLooper())
 * val scheduler = Scheduler(handler)
 * ```
 *
 * Background thread scheduler:
 * ```
 * val handlerThread = HandlerThread("AdScheduler")
 * handlerThread.start()
 * val scheduler = Scheduler(Handler(handlerThread.looper))
 * ```
 */
@Stable
actual class Scheduler(handler: Handler?) {

    private var handler: Handler = handler ?: Handler(Looper.getMainLooper())

    /**
     * Maps Kotlin tasks to their Runnable wrappers
     * so they can be removed later safely.
     */
    private val runnableMap = ConcurrentHashMap<() -> Unit, Runnable>()

    /**
     * Secondary constructor that defaults to the Android main thread.
     */
    actual constructor() : this(Handler(Looper.getMainLooper()))

    /**
     * Schedules a task to execute after a specified delay.
     *
     * @param delayMillis Delay duration in milliseconds.
     * @param task Task to execute.
     */
    actual fun schedule(delayMillis: Long, task: () -> Unit) {
        handler.postDelayed(task, delayMillis)
    }

    /**
     * Cancels a previously scheduled task.
     *
     * @param task The same task reference used in [schedule].
     */
    actual fun cancel(task: () -> Unit) {
        val runnable = runnableMap.remove(task) ?: return
        handler.removeCallbacks(runnable)
    }
}