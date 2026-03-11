package io.github.saifullah.nurani.ads.core.compose

import androidx.compose.runtime.ProvidableCompositionLocal

expect val LocalPlatformActivity : ProvidableCompositionLocal<PlatformActivity?>
expect val LocalPlatformContext: ProvidableCompositionLocal<PlatformContext>