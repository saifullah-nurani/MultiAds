package io.github.saifullah.nurani.ads.core.compose

enum class Platform {
    IOS, Android
}

expect fun isPlatformAndroid(): Boolean
expect fun isPlatformIos(): Boolean
expect fun getPlatform(): Platform