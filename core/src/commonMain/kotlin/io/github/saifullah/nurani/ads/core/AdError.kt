package io.github.saifullah.nurani.ads.core

import kotlin.reflect.KClass
import kotlin.reflect.cast

class AdError(
    /** Machine-readable error code (SDK-defined).  */
    val code: Int,
    /** Human-readable message.  */
    val message: String?,
    /** Original error from network / SDK / system (optional).  */
    val originalError: Any? = null,
    /** Optional root cause (Throwable).  */
    val cause: Throwable? = null
) {
    /**
     * Creates AdFailure.
     */

    /**
     * Returns true if this failure wraps a specific error type.
     */
    fun <T : Any> isFrom(type: KClass<T>): Boolean {
        return type.isInstance(originalError)
    }

    /**
     * Safely returns the original error if it matches requested type.
     */
    fun <T : Any> getOriginalAs(type: KClass<T>): T? {
        return if (type.isInstance(originalError))  type.cast(originalError) else  null
    }

    override fun toString(): String {
        return "AdError{" +
                "code=" + code +
                ", message='" + message + '\'' +
                ", originalError=" + originalError +
                ", cause=" + cause +
                '}'
    }
}