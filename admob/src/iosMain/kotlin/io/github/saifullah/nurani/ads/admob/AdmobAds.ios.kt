package io.github.saifullah.nurani.ads.admob

import io.github.saifullah.nurani.ads.core.compose.PlatformContext
import GoogleMobileAds.GADMobileAds
import kotlinx.cinterop.ExperimentalForeignApi

actual object AdmobAds {
    @OptIn(ExperimentalForeignApi::class)
    actual fun init(context: PlatformContext) {
        GADMobileAds.sharedInstance().startWithCompletionHandler(null)
    }
}
