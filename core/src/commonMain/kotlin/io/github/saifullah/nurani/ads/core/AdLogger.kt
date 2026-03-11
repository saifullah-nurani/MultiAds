package io.github.saifullah.nurani.ads.core

interface AdLogger {

    /** Log debug message.  */
    fun d(message: String?)

    /** Log error message (highest priority).  */
    fun e(message: String?)

    companion object {
        // ---------------------------------------------------------
        // Optional: No-op default implementation (fully silent)
        // ---------------------------------------------------------
        val NONE: AdLogger = object : AdLogger {
            override fun d(message: String?) {}
            override fun e(message: String?) {}
        }
    }
}