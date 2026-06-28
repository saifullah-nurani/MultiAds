package io.github.saifullah.nurani.ads.multi

import io.github.saifullah.nurani.ads.core.AdLoadCallback
import io.github.saifullah.nurani.ads.core.AdContentCallback
import io.github.saifullah.nurani.ads.core.BannerAdListener
import io.github.saifullah.nurani.ads.core.AdError

fun createLoadCallback(network: String, format: String): AdLoadCallback {
    return AdLoadCallback(
        onAdLoaded = { println("[$network] [LoadCallback] $format loaded successfully") },
        onAdFailedToLoad = { error -> println("[$network] [LoadCallback] $format failed to load: ${error?.message ?: "Unknown"}") }
    )
}

fun createContentCallback(network: String, format: String): AdContentCallback {
    return AdContentCallback(
        onAdShowed = { println("[$network] [ContentCallback] $format showed") },
        onAdDisplayed = { println("[$network] [ContentCallback] $format displayed") },
        onAdDismissed = { println("[$network] [ContentCallback] $format dismissed") },
        onAdClicked = { println("[$network] [ContentCallback] $format clicked") },
        onAdFailedToShow = { error -> println("[$network] [ContentCallback] $format failed to show: ${error?.message ?: "Unknown"}") }
    )
}

fun createBannerListener(network: String): BannerAdListener {
    return BannerAdListener(
        onAdLoaded = { println("[$network] [BannerListener] Banner loaded successfully") },
        onAdFailedToLoad = { error -> println("[$network] [BannerListener] Banner failed to load: ${error?.message ?: "Unknown"}") },
        onAdClicked = { println("[$network] [BannerListener] Banner clicked") },
        onAdDisplayed = { println("[$network] [BannerListener] Banner displayed") },
        onAdDismissed = { println("[$network] [BannerListener] Banner dismissed") }
    )
}
