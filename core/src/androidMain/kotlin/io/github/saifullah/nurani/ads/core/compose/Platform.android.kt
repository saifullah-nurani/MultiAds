package io.github.saifullah.nurani.ads.core.compose

actual fun isPlatformAndroid(): Boolean {
    return true
}

actual fun isPlatformIos(): Boolean {
    return false
}

actual fun getPlatform(): Platform {
    return Platform.Android
}
