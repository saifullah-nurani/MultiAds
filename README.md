# MultiAds
MultiAds is a lightweight and flexible advertising SDK designed to simplify ad integration in modern mobile applications. Built with performance and developer experience in mind, MultiAds provides an easy way to integrate ads while maintaining full control over loading strategies, retries, and lifecycle management.

A professional, visual-first Kotlin Multiplatform library for managing Admob ads in Compose Multiplatform (Android & iOS).

## Features
- **Compose Multiplatform Support**: Pure Compose API for both platforms.
- **Visual State Indicators**: Built-in indicators for Loading, Refreshing, Reloading, and Errors.
- **Robust State Management**: Automatic retries and reactive ad switching.
- **Easy Integration**: Simple setup for Interstitials, Rewarded, and Banner ads.

## Installation

### Maven Central (Android/Common)
Add the dependency to your `commonMain` source set:

```kotlin
kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("io.github.saifullah-nurani.ads:admob:1.0.0")
            }
        }
    }
}
```

### CocoaPods (iOS)
Add to your `Podfile`:

```ruby
pod 'MultiAdsAdmob', '~> 1.0.0'
```

*Note: CocoaPods is required for iOS development and generating podspecs. If you don't have it installed, you can disable the plugin in `gradle.properties` by setting `PROJECT_ENABLE_COCOAPODS=false`.*

**To install CocoaPods on macOS:**
```bash
sudo gem install cocoapods
```

### Swift Package Manager (iOS)
Add the following URL to your Xcode project's package dependencies:
`https://github.com/saifullah-nurani/MultiAds.git`

## Usage Example

```kotlin
val adState = rememberAdmobInterstitialAd(
    properties = AdmobAdProperties(
        androidAdUnitId = "your-android-id",
        iosAdUnitId = "your-ios-id"
    )
)

Button(onClick = { adState.showAd(activity) }) {
    StatusIndicator(adState)
}
```

## License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
