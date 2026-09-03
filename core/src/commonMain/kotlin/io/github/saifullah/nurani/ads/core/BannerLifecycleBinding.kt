package io.github.saifullah.nurani.ads.core

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.LifecycleStartEffect

@Stable
class BannerLifecycleBinding<T> internal constructor() {
    internal var currentView by mutableStateOf<T?>(null)
        private set

    fun attach(view: T) {
        currentView = view
    }

    fun detach(view: T) {
        if (currentView === view) currentView = null
    }
}

@Composable
fun <T> rememberBannerLifecycleBinding(
    onStart: (T) -> Unit,
    onStop: (T) -> Unit
): BannerLifecycleBinding<T> {
    val binding = remember { BannerLifecycleBinding<T>() }
    val currentOnStart by rememberUpdatedState(onStart)
    val currentOnStop by rememberUpdatedState(onStop)
    val view = binding.currentView

    LifecycleStartEffect(view) {
        if (view != null) currentOnStart(view)
        onStopOrDispose {
            if (view != null) currentOnStop(view)
        }
    }

    return binding
}
