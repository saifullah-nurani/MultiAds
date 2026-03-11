package io.github.saifullah.nurani.ads.core.compose

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalWithComputedDefaultOf
import androidx.compose.ui.platform.LocalContext

actual val LocalPlatformActivity: ProvidableCompositionLocal<PlatformActivity?> =
    compositionLocalWithComputedDefaultOf { findOwner(LocalContext.currentValue) }

actual val LocalPlatformContext  get() = LocalContext