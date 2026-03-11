package io.github.saifullah.nurani.ads.core.compose

actual fun isPlatformAndroid(): Boolean {
    return false
}

actual fun isPlatformIos(): Boolean {
    return true
}

actual fun getPlatform(): Platform {
    return Platform.IOS
}

