package io.github.saifullah.nurani.ads.core.utils

import android.app.Activity
import android.content.Context
import io.github.saifullah.nurani.ads.core.compose.findOwner

class ContextUtils private constructor(){
    companion object{
        @JvmStatic
        fun findActivity(context: Context): Activity?{
            return findOwner(context)
        }
    }
}