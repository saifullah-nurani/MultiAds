package io.github.saifullah.nurani.ads.admob

import io.github.saifullah.nurani.ads.core.compose.PlatformContext

expect object AdmobAds {
    fun init(context: PlatformContext)
}
