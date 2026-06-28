<div align="center">

# 💎 MultiAds

**A Premium, Lightweight & Mediated Kotlin Multiplatform Ad Library for Android and iOS**

[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-blue.svg?style=for-the-badge&logo=kotlin)](http://kotlinlang.org)
[![KMP](https://img.shields.io/badge/KMP-Supported-purple.svg?style=for-the-badge&logo=kotlin)](https://kotlinlang.org/docs/multiplatform.html)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.saifullah-nurani.ads/multi-ads?style=for-the-badge&color=brightgreen)](https://central.sonatype.com/artifact/io.github.saifullah-nurani.ads/multi-ads)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg?style=for-the-badge)](https://opensource.org/licenses/MIT)

---

🤖 **Android (SDK 21+)** &nbsp;•&nbsp; 🍏 **iOS (14.1+)** &nbsp;•&nbsp; 🎨 **Compose Multiplatform** &nbsp;•&nbsp; ⚡ **Waterfall Mediation**

</div>

`MultiAds` provides a complete, performance-optimized, and unified solution to load and show ads across both Android and iOS platforms. It wraps official native SDKs to ensure maximum rendering reliability and ad delivery rates.

### Key Features 🌟

- **⚡ Unified Mediation (Waterfall)**: Prioritized loading, concurrent network requests, and automatic fallbacks on ad load failure.
- **🎨 Compose Multiplatform UI**: Ready-to-use Composable UI components for Banner ads (`MultiBannerAd`) and remember-hooks for fullscreen formats.
- **💎 Pure Platform Native Binding**: Direct integration via native views (`UIView` for iOS, native `FrameLayout` for Android) preventing container rendering issues (like white background/recomposition overlaps).
- **📦 Modular Design**: Standalone sub-modules for AdMob, AppLovin, Pangle, Vungle, InMobi, IronSource, and Meta if you don't need waterfall mediation.
- **🔄 Auto-Orchestrated Lifecycle**: Automatic auto-reload policy management, state restoration, and background state handling.
- **📝 Real-time Log Console**: Tagged diagnostic logger (`DefaultAdLogger`) with customizable print callbacks.

---

## Modules

| Module | Artifact Name | Supported Formats | Description |
| :--- | :--- | :--- | :--- |
| **Core** | `core` | Shared abstractions | Shared configurations, retry/refresh policies, callback managers |
| **AdMob** | `admob` | Banner, Interstitial, Rewarded, Rewarded Interstitial, App Open | Google Mobile Ads SDK implementation |
| **AppLovin** | `applovin` | Banner, Interstitial, Rewarded, App Open | AppLovin MAX SDK implementation |
| **Pangle** | `pangle` | Banner, Interstitial, Rewarded, App Open | ByteDance PAGAdSDK implementation |
| **Vungle** | `vungle` | Banner, Interstitial, Rewarded | Liftoff Vungle Ads SDK implementation |
| **InMobi** | `inmobi` | Banner, Interstitial, Rewarded | InMobi Ads SDK implementation |
| **IronSource** | `ironsource` | Banner, Interstitial, Rewarded | Unity IronSource SDK implementation |
| **Meta** | `man` | Banner, Interstitial, Rewarded | Meta Audience Network SDK implementation |
| **Waterfall** | `multi-ads` | Mediation Banner, Interstitial, Rewarded, App Open | Priority-concurrency mediated waterfall orchestration |

---

## Version

The latest release version for all multiplatform artifacts:

```kotlin
val multiAdsVersion = "1.1.7"
```

## Repositories

Some ad SDK repositories must be added in the consumer app:

```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven(url = "https://artifact.bytedance.com/repository/pangle/")
        maven(url = "https://android-sdk.is.com/")
    }
}
```

## Installation

### KMM / Compose Multiplatform

Add modules in `commonMain`:

```kotlin
sourceSets {
    commonMain.dependencies {
        implementation("io.github.saifullah-nurani.ads:multi-ads:1.1.7")

        // Optional standalone modules
        implementation("io.github.saifullah-nurani.ads:admob:1.1.7")
        implementation("io.github.saifullah-nurani.ads:applovin:1.1.7")
        implementation("io.github.saifullah-nurani.ads:pangle:1.1.7")
        implementation("io.github.saifullah-nurani.ads:vungle:1.1.7")
        implementation("io.github.saifullah-nurani.ads:inmobi:1.1.7")
        implementation("io.github.saifullah-nurani.ads:ironsource:1.1.7")
        implementation("io.github.saifullah-nurani.ads:man:1.1.7")
    }
}
```

### Android-only

Use Android AAR variants:

```kotlin
dependencies {
    implementation("io.github.saifullah-nurani.ads:multi-ads-android:1.1.7")

    // Optional standalone modules
    implementation("io.github.saifullah-nurani.ads:admob-android:1.1.7")
    implementation("io.github.saifullah-nurani.ads:applovin-android:1.1.7")
    implementation("io.github.saifullah-nurani.ads:pangle-android:1.1.7")
    implementation("io.github.saifullah-nurani.ads:vungle-android:1.1.7")
    implementation("io.github.saifullah-nurani.ads:inmobi-android:1.1.7")
    implementation("io.github.saifullah-nurani.ads:ironsource-android:1.1.7")
    implementation("io.github.saifullah-nurani.ads:man-android:1.1.7")
}
```

## R8 / ProGuard

`consumer-rules.pro` is already bundled in every published module.

That means:

- public models/config classes are already kept
- module API packages are already protected
- required SDK keep rules are already included

In normal usage you do **not** need to manually copy extra ProGuard rules from this repo into your app.

## Initialization

Initialize each SDK once before loading ads.

### Compose commonMain

```kotlin
val admobReady = rememberAdmobAdsInit(
    androidAppId = "ANDROID_APP_ID",
    iosAppId = "IOS_APP_ID"
)

val appLovinReady = rememberAppLovinAdsInit(
    androidSdkKey = "ANDROID_SDK_KEY",
    iosSdkKey = "IOS_SDK_KEY"
)

val pangleReady = rememberPangleAdsInit(
    androidAppId = "ANDROID_APP_ID",
    iosAppId = "IOS_APP_ID"
)

val vungleReady = rememberVungleAdsInit(
    androidAppId = "ANDROID_APP_ID",
    iosAppId = "IOS_APP_ID"
)

val inMobiReady = rememberInMobiAdsInit(
    androidAccountId = "ANDROID_ACCOUNT_ID",
    iosAccountId = "IOS_ACCOUNT_ID"
)

val ironSourceReady = rememberIronSourceAdsInit(
    androidAppKey = "ANDROID_APP_KEY",
    iosAppKey = "IOS_APP_KEY"
)

val metaReady = rememberMetaAudienceNetworkAdsInit()
```

### Shared non-Compose init

```kotlin
AdmobAds.init(context)

AppLovinAds.init(
    context = context,
    androidSdkKey = "ANDROID_SDK_KEY",
    iosSdkKey = "IOS_SDK_KEY"
)

PangleAds.init(
    context = context,
    androidAppId = "ANDROID_APP_ID",
    iosAppId = "IOS_APP_ID"
)

VungleAds.init(
    context = context,
    androidAppId = "ANDROID_APP_ID",
    iosAppId = "IOS_APP_ID"
)

InMobiAds.init(
    context = context,
    androidAccountId = "ANDROID_ACCOUNT_ID",
    iosAccountId = "IOS_ACCOUNT_ID"
)

IronSourceAds.init(
    context = context,
    androidAppKey = "ANDROID_APP_KEY",
    iosAppKey = "IOS_APP_KEY"
)

MetaAudienceNetworkAds.init(context)
```

## Shared property helpers

All standalone modules support a request `tag`.

```kotlin
import io.github.saifullah.nurani.ads.admob.compose.admobAdProperties
import io.github.saifullah.nurani.ads.applovin.compose.appLovinAdProperties
import io.github.saifullah.nurani.ads.pangle.compose.pangleAdProperties
import io.github.saifullah.nurani.ads.vungle.compose.vunglePlacementProperties
import io.github.saifullah.nurani.ads.inmobi.compose.inMobiPlacementProperties
import io.github.saifullah.nurani.ads.ironsource.compose.ironSourceAdProperties
import io.github.saifullah.nurani.ads.man.compose.metaPlacementProperties

val admob = admobAdProperties(
    androidAdUnitId = "ANDROID_ID",
    iosAdUnitId = "IOS_ID",
    tag = "session-123"
)

val appLovin = appLovinAdProperties(
    androidAdUnitId = "ANDROID_ID",
    iosAdUnitId = "IOS_ID",
    tag = "session-123"
)

val pangle = pangleAdProperties(
    androidAdUnitId = "ANDROID_ID",
    iosAdUnitId = "IOS_ID",
    tag = "session-123"
)

val vungle = vunglePlacementProperties(
    androidPlacementId = "ANDROID_PLACEMENT",
    iosPlacementId = "IOS_PLACEMENT",
    tag = "session-123"
)

val inmobi = inMobiPlacementProperties(
    androidPlacementId = 123456789L,
    iosPlacementId = 987654321L,
    tag = "session-123"
)

val ironSource = ironSourceAdProperties(
    androidPlacementName = "ANDROID_PLACEMENT_NAME",
    iosPlacementName = "IOS_AD_UNIT_OR_PLACEMENT",
    tag = "session-123"
)

val meta = metaPlacementProperties(
    androidPlacementId = "ANDROID_PLACEMENT_ID",
    iosPlacementId = "IOS_PLACEMENT_ID",
    tag = "session-123"
)
```

## Standalone module usage

### AdMob

```kotlin
import io.github.saifullah.nurani.ads.admob.compose.AdmobBannerAd
import io.github.saifullah.nurani.ads.admob.compose.rememberAdmobInterstitialAd
import io.github.saifullah.nurani.ads.admob.compose.rememberAdmobRewardedAd
import io.github.saifullah.nurani.ads.admob.compose.rememberAdmobRewardedInterstitialAd

val interstitial = rememberAdmobInterstitialAd(properties = admob)
val rewarded = rememberAdmobRewardedAd(properties = admob)
val rewardedInterstitial = rememberAdmobRewardedInterstitialAd(properties = admob)

AdmobBannerAd(
    properties = admob,
    testModeEnabled = true,
    expandWhenReady = true
)
```

### AppLovin

```kotlin
import io.github.saifullah.nurani.ads.applovin.compose.AppLovinBannerAd
import io.github.saifullah.nurani.ads.applovin.compose.rememberAppLovinInterstitialAd
import io.github.saifullah.nurani.ads.applovin.compose.rememberAppLovinRewardedAd

val interstitial = rememberAppLovinInterstitialAd(properties = appLovin)
val rewarded = rememberAppLovinRewardedAd(properties = appLovin)

AppLovinBannerAd(
    properties = appLovin,
    testModeEnabled = true,
    expandWhenReady = true
)
```

### Pangle

```kotlin
import io.github.saifullah.nurani.ads.pangle.compose.PangleBannerAd
import io.github.saifullah.nurani.ads.pangle.compose.rememberPangleInterstitialAd
import io.github.saifullah.nurani.ads.pangle.compose.rememberPangleRewardedAd

val interstitial = rememberPangleInterstitialAd(properties = pangle)
val rewarded = rememberPangleRewardedAd(properties = pangle)

PangleBannerAd(
    properties = pangle,
    testModeEnabled = true,
    expandWhenReady = true
)
```

### Vungle

```kotlin
import io.github.saifullah.nurani.ads.vungle.compose.VungleBannerAd
import io.github.saifullah.nurani.ads.vungle.compose.rememberVungleInterstitialAd
import io.github.saifullah.nurani.ads.vungle.compose.rememberVungleRewardedAd

val interstitial = rememberVungleInterstitialAd(properties = vungle)
val rewarded = rememberVungleRewardedAd(properties = vungle)

VungleBannerAd(
    properties = vungle,
    testModeEnabled = true,
    expandWhenReady = true
)
```

### InMobi

```kotlin
import io.github.saifullah.nurani.ads.inmobi.compose.InMobiBannerAd
import io.github.saifullah.nurani.ads.inmobi.compose.rememberInMobiInterstitialAd
import io.github.saifullah.nurani.ads.inmobi.compose.rememberInMobiRewardedAd

val interstitial = rememberInMobiInterstitialAd(properties = inmobi)
val rewarded = rememberInMobiRewardedAd(properties = inmobi)

InMobiBannerAd(
    properties = inmobi,
    testModeEnabled = true,
    expandWhenReady = true
)
```

### IronSource

```kotlin
import io.github.saifullah.nurani.ads.ironsource.compose.IronSourceBannerAd
import io.github.saifullah.nurani.ads.ironsource.compose.rememberIronSourceInterstitialAd
import io.github.saifullah.nurani.ads.ironsource.compose.rememberIronSourceRewardedAd

val interstitial = rememberIronSourceInterstitialAd(properties = ironSource)
val rewarded = rememberIronSourceRewardedAd(properties = ironSource)

IronSourceBannerAd(
    properties = ironSource,
    testModeEnabled = true,
    expandWhenReady = true
)
```

### Meta

```kotlin
import io.github.saifullah.nurani.ads.man.compose.MetaBannerAd
import io.github.saifullah.nurani.ads.man.compose.rememberMetaInterstitialAd
import io.github.saifullah.nurani.ads.man.compose.rememberMetaRewardedAd

val interstitial = rememberMetaInterstitialAd(properties = meta)
val rewarded = rememberMetaRewardedAd(properties = meta)

MetaBannerAd(
    properties = meta,
    testModeEnabled = true,
    expandWhenReady = true
)
```

## Waterfall module (`multi-ads`)

`multi-ads` loads networks by priority and can keep multiple requests active concurrently.

### Waterfall config

```kotlin
import io.github.saifullah.nurani.ads.multi.models.waterfallConfig

val waterfall = waterfallConfig {
    maxConcurrentLoads(2)
    admob("ADMOB_UNIT_ID", priority = 1)
    applovin("APPLOVIN_UNIT_ID", priority = 2)
    pangle("PANGLE_UNIT_ID", priority = 3)
    vungle("VUNGLE_PLACEMENT_ID", priority = 4)
    inmobi(123456789L, priority = 5)
    ironsource("IRONSOURCE_PLACEMENT_NAME", priority = 6)
}
```

### Interstitial

```kotlin
import io.github.saifullah.nurani.ads.multi.compose.rememberMultiInterstitialAd

val interstitial = rememberMultiInterstitialAd(
    waterfallConfig = waterfall,
    testModeEnabled = true,
    initialLoad = true,
    tag = "session-123"
)
```

### Rewarded

```kotlin
import io.github.saifullah.nurani.ads.multi.compose.rememberMultiRewardedAd

val rewarded = rememberMultiRewardedAd(
    waterfallConfig = waterfall,
    testModeEnabled = true,
    initialLoad = true,
    tag = "session-123",
    onUserRewarded = {
        // reward user
    }
)
```

### Banner

Simple overload:

```kotlin
import io.github.saifullah.nurani.ads.core.AdSize
import io.github.saifullah.nurani.ads.core.BannerAd
import io.github.saifullah.nurani.ads.multi.compose.MultiBannerAd

MultiBannerAd(
    waterfallConfig = waterfall,
    testModeEnabled = true,
    expandWhenReady = true,
    animateExpansion = true,
    adSize = BannerAd.Fixed(AdSize.BANNER),
    tag = "banner-session"
)
```

Advanced DSL config:

```kotlin
import io.github.saifullah.nurani.ads.core.AdError
import io.github.saifullah.nurani.ads.core.AdSize
import io.github.saifullah.nurani.ads.core.BannerAd
import io.github.saifullah.nurani.ads.core.BannerAdListener
import io.github.saifullah.nurani.ads.multi.compose.MultiBannerAd
import io.github.saifullah.nurani.ads.multi.models.AdNetworkConfig
import io.github.saifullah.nurani.ads.multi.models.MultiAdListener
import io.github.saifullah.nurani.ads.multi.models.multiBannerAdConfig

MultiBannerAd(
    waterfallConfig = waterfall,
    config = multiBannerAdConfig {
        testModeEnabled = true
        expandWhenReady = true
        animateExpansion = true
        adSize = BannerAd.Fixed(AdSize.BANNER)
        tag = "banner-session"
        adListener = BannerAdListener(
            onAdLoaded = { println("banner loaded") },
            onAdFailedToLoad = { println("banner failed: ${it?.message}") }
        )
    },
    adListener = object : MultiAdListener {
        override fun onAdLoaded(network: AdNetworkConfig) {
            println("loaded from ${network.network}")
        }

        override fun onAdFailedToLoad(network: AdNetworkConfig, error: AdError?) {
            println("${network.network} failed: ${error?.message}")
        }
    }
)
```

### Shared waterfall config object

Use `MultiAdsConfig` when you want one place for tag/logger/retry/refresh/test mode:

```kotlin
import io.github.saifullah.nurani.ads.multi.models.multiAdsConfig

val config = multiAdsConfig {
    isTestModeEnabled = true
    tag = "global-session"
    waterfall {
        maxConcurrentLoads(2)
        admob("ADMOB_UNIT_ID", 1)
        applovin("APPLOVIN_UNIT_ID", 2)
    }
}
```

Then:

```kotlin
import io.github.saifullah.nurani.ads.multi.compose.MultiBannerAd
import io.github.saifullah.nurani.ads.multi.compose.rememberMultiInterstitialAd
import io.github.saifullah.nurani.ads.multi.compose.rememberMultiRewardedAd
import io.github.saifullah.nurani.ads.multi.models.multiBannerAdConfig

val interstitial = rememberMultiInterstitialAd(multiAdsConfig = config)
val rewarded = rememberMultiRewardedAd(multiAdsConfig = config)

MultiBannerAd(
    multiAdsConfig = config,
    config = multiBannerAdConfig {
        expandWhenReady = true
    }
)
```

### Multi callbacks

`multi-ads` supports network-aware callbacks:

- `MultiAdLoadCallback`
- `MultiAdContentCallback`
- `MultiAdListener` for banners

These callbacks tell you which network actually loaded or showed the ad.

### App open support in waterfall

`MultiAppOpenAd` currently supports:

- AdMob
- AppLovin

Example:

```kotlin
val appOpenWaterfall = waterfallConfig {
    maxConcurrentLoads(2)
    admob("ADMOB_APP_OPEN_ID", 1)
    applovin("APPLOVIN_APP_OPEN_ID", 2)
}

val appOpenAd = MultiAppOpenAd(context).apply {
    waterfallConfig = appOpenWaterfall
    testModeEnabled = true
    tag = "app-open-session"
}

appOpenAd.loadAd()
```

## Native app-open auto show

Compose app-open helpers were removed. Use native registration APIs instead.

### Android

Available in:

- `AdmobNativeAppOpen`
- `AppLovinNativeAppOpen`
- `PangleNativeAppOpen`
- `MultiNativeAppOpen`

Example:

```kotlin
import android.app.Application
import androidx.lifecycle.Lifecycle
import io.github.saifullah.nurani.ads.admob.AdmobNativeAppOpen

AdmobNativeAppOpen.register(
    application = application,
    adUnitId = "APP_OPEN_ID",
    initialLoad = true,
    testModeEnabled = true
) {
    anyActivity()
    // or:
    // activity<MainActivity>(Lifecycle.Event.ON_RESUME)
    // fragment<HomeFragment>(Lifecycle.Event.ON_RESUME)
}
```

### iOS

Available in:

- `AdmobNativeAppOpen`
- `AppLovinNativeAppOpen`
- `PangleNativeAppOpen`
- `MultiNativeAppOpen`

Example:

```kotlin
import io.github.saifullah.nurani.ads.admob.AdmobNativeAppOpen
import io.github.saifullah.nurani.ads.core.IosAppOpenLifecycleState

val handle = AdmobNativeAppOpen.register(
    adUnitId = "APP_OPEN_ID",
    initialLoad = true,
    testModeEnabled = true
) {
    appDidBecomeActive()
    // or:
    // appWillEnterForeground()
    // viewController<MyViewController>(IosAppOpenLifecycleState.VIEW_DID_APPEAR)
}
```

If you use view-controller based rules, forward lifecycle manually:

```kotlin
handle.notifyViewWillAppear(viewController)
handle.notifyViewDidAppear(viewController)
```

Dispose when no longer needed:

```kotlin
handle.dispose()
```

## Android SDK setup reminders

Some SDKs still require their normal manifest / plist setup:

- AdMob: `APPLICATION_ID` in AndroidManifest and `GADApplicationIdentifier` in `Info.plist`
- AppLovin: SDK key in Android manifest / placeholders and `Info.plist`
- Pangle: app id initialization before load
- Vungle: app id initialization before load
- InMobi: account id initialization before load
- IronSource: app key initialization before load

## Sample app

The `sample` module contains working showcase screens for:

- standalone networks
- multi-network waterfall
- banner, interstitial, rewarded, and app-open scenarios

## License

MIT. See [LICENSE](LICENSE).
