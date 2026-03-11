package io.github.saifullah.nurani.ads.core.compose

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalWithComputedDefaultOf
import androidx.compose.runtime.staticCompositionLocalOf
import platform.UIKit.UIApplication


actual val LocalPlatformContext: ProvidableCompositionLocal<PlatformContext> = staticCompositionLocalOf { object :PlatformContext(){} }

actual val LocalPlatformActivity: ProvidableCompositionLocal<PlatformActivity?> get() = compositionLocalWithComputedDefaultOf {  UIApplication.sharedApplication.keyWindow?.rootViewController()  }