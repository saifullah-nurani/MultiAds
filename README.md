<div align="center">
  
# 🚀 MultiAds

**A Professional Kotlin Multiplatform Ad Management Library**

[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.0-blue.svg?logo=kotlin)](http://kotlinlang.org)
[![KMP](https://img.shields.io/badge/KMP-Supported-purple.svg?logo=kotlin)](https://kotlinlang.org/docs/multiplatform.html)
[![Version](https://img.shields.io/maven-central/v/io.github.saifullah-nurani.ads/multi-ads)](https://central.sonatype.com/artifact/io.github.saifullah-nurani.ads/multi-ads)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Android Supported](https://img.shields.io/badge/Platform-Android-3DDC84.svg?logo=android)](https://developer.android.com)
[![iOS Supported](https://img.shields.io/badge/Platform-iOS-000000.svg?logo=apple)](https://developer.apple.com)

**MultiAds** is a lightweight, flexible, and robust ad management solution designed to simplify the complex world of ad integration across **Android** and **iOS** applications. 

[Features](#-key-features) • [Installation](#-installation-dependencies) • [Module Setup](#%EF%B8%8F-module-setup--implementation-guides) • [License](#-license)

</div>

---

It provides standard wrappers for multiple popular ad networks, alongside a unified, priority-based **Waterfall Mediation module (`multi-ads`)** that handles dynamic networks fallback, automatic retry policies, and ad lifecycles out-of-the-box.

---

## 🚀 Key Features

* **Multi-Network Support:** Pre-configured modules for **AdMob**, **AppLovin (MAX)**, **Pangle**, **Vungle (Liftoff)**, **InMobi**, and **IronSource**.
* **Waterfall Mediation (`multi-ads`):** Easily configure ad fallbacks with customizable concurrent loaders and priority rankings.
* **Unified AdState API:** Standardized API lifecycle callbacks.
* **Multi-Paradigm Support:** Seamlessly works with **Android Java**, **Android Kotlin (View-based)**, **Compose Multiplatform (KMM)**, and **iOS Compose**.

---

## 📦 Installation Dependencies

> [!IMPORTANT]
> **Required Repositories:** Gradle ignores custom repositories defined in library POM files. To resolve transitive SDK dependencies (like Pangle and IronSource), you **MUST** explicitly add their Maven repositories to your consumer project's `settings.gradle.kts` (or root `build.gradle.kts`) file:

```kotlin
// settings.gradle.kts
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // Required for Pangle SDK
        maven { url = uri("https://artifact.bytedance.com/repository/pangle/") }
        // Required for IronSource SDK
        maven { url = uri("https://android-sdk.is.com/") }
    }
}
```

Choose the dependency format based on your project type.

### 1. Compose Multiplatform / KMM
Add dependencies to your `commonMain` source set:
```kotlin
sourceSets {
    commonMain.dependencies {
        // All-in-One Waterfall Mediation (Recommended)
        implementation("io.github.saifullah-nurani.ads:multi-ads:1.1.2")
        
        // Standalone Modules
        // implementation("io.github.saifullah-nurani.ads:core:1.1.2")
        // implementation("io.github.saifullah-nurani.ads:admob:1.1.2")
        // implementation("io.github.saifullah-nurani.ads:applovin:1.1.2")
        // implementation("io.github.saifullah-nurani.ads:pangle:1.1.2")
        // implementation("io.github.saifullah-nurani.ads:vungle:1.1.2")
        // implementation("io.github.saifullah-nurani.ads:inmobi:1.1.2")
        // implementation("io.github.saifullah-nurani.ads:is:1.1.2")
    }
}
```

### 2. Pure Android (Java / Kotlin View-based)
If you are building a standard Android app (non-KMP), append the `-android` suffix to ensure you get the Android-specific AARs:
```kotlin
dependencies {
    implementation("io.github.saifullah-nurani.ads:multi-ads-android:1.1.2")
    
    // Standalone Modules
    // implementation("io.github.saifullah-nurani.ads:core-android:1.1.2")
    // implementation("io.github.saifullah-nurani.ads:admob-android:1.1.2")
    // implementation("io.github.saifullah-nurani.ads:applovin-android:1.1.2")
    // implementation("io.github.saifullah-nurani.ads:pangle-android:1.1.2")
    // implementation("io.github.saifullah-nurani.ads:vungle-android:1.1.2")
    // implementation("io.github.saifullah-nurani.ads:inmobi-android:1.1.2")
    // implementation("io.github.saifullah-nurani.ads:is-android:1.1.2")
}
```

### 3. iOS Setup (CocoaPods)
```ruby
pod 'MultiAdsMulti', '~> 1.1.2'
# pod 'MultiAdsAdmob', '~> 1.1.2'
```

---

## 🛠️ Module Setup & Implementation Guides

Below are the detailed setup and implementation steps for every ad format, broken down by module and programming paradigm.

---

### 1. Waterfall Mediation (`multi-ads`)
The `multi-ads` module handles all networks. You simply configure a `WaterfallConfig`.

#### Interstitial Ad
**Compose (KMP / Android / iOS):**
```kotlin
val waterfall = remember { waterfallConfig { admob("ID", 1); applovin("ID", 2) } }
val ad = rememberMultiInterstitialAd(waterfall, true)
Button(onClick = { ad.showAd(activity) }) { Text("Show Interstitial") }
```
**Android Kotlin (View):**
```kotlin
val waterfall = waterfallConfig { admob("ID", 1); applovin("ID", 2) }
val ad = MultiInterstitialAd(context).apply { waterfallConfig = waterfall; loadAd() }
// showAd(activity) when loaded
```
**Android Java:**
```java
MultiInterstitialAd ad = new MultiInterstitialAd(context);
ad.setWaterfallConfig(waterfall); // Configured via builder
ad.loadAd();
// ad.showAd(activity);
```

#### Rewarded Ad
**Compose:** `val ad = rememberMultiRewardedAd(waterfall, true)`
**Android Kotlin:** `val ad = MultiRewardedAd(context)`
**Android Java:** `MultiRewardedAd ad = new MultiRewardedAd(context);`

#### App Open Ad
**Compose:** `val ad = rememberMultiAppOpenAd(waterfall, true)`
**Android Kotlin:** `val ad = MultiAppOpenAd(context)`
**Android Java:** `MultiAppOpenAd ad = new MultiAppOpenAd(context);`

#### Banner Ad
**Compose:**
```kotlin
MultiBannerAd(waterfallConfig = waterfall, modifier = Modifier.fillMaxWidth())
```
**Android Kotlin / Java:**
```kotlin
val bannerView = MultiBannerView(context)
bannerView.waterfallConfig = waterfall
bannerView.loadAd()
layout.addView(bannerView)
```

---

### 2. AdMob Module (`admob`)
**Setup Requirement:**
Android (`AndroidManifest.xml`):
```xml
<meta-data android:name="com.google.android.gms.ads.APPLICATION_ID" android:value="ca-app-pub-xxx~yyy"/>
```
iOS (`Info.plist`):
```xml
<key>GADApplicationIdentifier</key><string>ca-app-pub-xxx~yyy</string>
```

#### Formats Implementation
**Interstitial:**
*   Compose: `rememberAdmobInterstitialAd(properties)`
*   Kotlin: `AdmobInterstitialAd.with(context, "ID").apply { loadAd() }`
*   Java: `AdmobInterstitialAd ad = AdmobInterstitialAd.with(context, "ID"); ad.loadAd();`

**Rewarded:**
*   Compose: `rememberAdmobRewardedAd(properties)`
*   Kotlin/Java: `AdmobRewardedAd.with(context, "ID")`

**App Open:**
*   Compose: `rememberAdmobAppOpenAd(properties)`
*   Kotlin/Java: `AdmobAppOpenAd.with(context, "ID")`

**Banner:**
*   Compose: `AdmobBannerAd(properties)`
*   Kotlin/Java: `AdmobBannerView(context).apply { adUnitId = "ID"; loadAd() }`

---

### 3. AppLovin Module (`applovin`)
**Setup Requirement:**
You must configure your AppLovin SDK Key. You can do this in your Android app's `build.gradle.kts` or `AndroidManifest.xml`.

**Option 1: Using `build.gradle.kts` (Recommended)**
Add the AppLovin Quality Service plugin (optional but recommended for MAX) and/or use manifest placeholders:
```kotlin
// app/build.gradle.kts
android {
    defaultConfig {
        manifestPlaceholders["applovin.sdk.key"] = "YOUR_SDK_KEY"
    }
}

// Optional: AppLovin Quality Service Plugin
// applovin {
//     apiKey = "YOUR_SDK_KEY"
// }
```

**Option 2: Android (`AndroidManifest.xml`)**
```xml
<meta-data android:name="applovin.sdk.key" android:value="YOUR_SDK_KEY"/>
```
iOS (`Info.plist`):
```xml
<key>AppLovinSDKKey</key><string>YOUR_SDK_KEY</string>
```

#### Formats Implementation
**Interstitial:**
*   Compose: `rememberAppLovinInterstitialAd(properties)`
*   Kotlin/Java: `AppLovinInterstitialAd.with(context, "ID")`

**Rewarded:**
*   Compose: `rememberAppLovinRewardedAd(properties)`
*   Kotlin/Java: `AppLovinRewardedAd.with(context, "ID")`

**App Open:**
*   Compose: `rememberAppLovinAppOpenAd(properties)`
*   Kotlin/Java: `AppLovinAppOpenAd.with(context, "ID")`

**Banner:**
*   Compose: `AppLovinBannerAd(properties)`
*   Kotlin/Java: `AppLovinBannerView(context).apply { adUnitId = "ID"; loadAd() }`

---

### 4. Pangle Module (`pangle`)
**Setup Requirement:** Requires explicit SDK initialization before loading ads.
**Init (Compose):** `val isInitialized = rememberPangleAdsInit("APP_ID")`
**Init (Kotlin/Java):** `PangleAds.init(context, "APP_ID", callback)`

#### Formats Implementation
**Interstitial:**
*   Compose: `rememberPangleInterstitialAd(properties)`
*   Kotlin/Java: `PangleInterstitialAd.with(context, "ID")`

**Rewarded:**
*   Compose: `rememberPangleRewardedAd(properties)`
*   Kotlin/Java: `PangleRewardedAd.with(context, "ID")`

**App Open:**
*   Compose: `rememberPangleAppOpenAd(properties)`
*   Kotlin/Java: `PangleAppOpenAd.with(context, "ID")`

**Banner:**
*   Compose: `PangleBannerAd(properties)`
*   Kotlin/Java: `PangleBannerView(context)`

---

### 5. Vungle (Liftoff) Module (`vungle`)
**Setup Requirement:** Requires SDK initialization.
**Init (Compose):** `val isInitialized = rememberVungleAdsInit("APP_ID")`
**Init (Kotlin/Java):** `VungleAds.init(context, "APP_ID", callback)`

#### Formats Implementation
**Interstitial:**
*   Compose: `rememberVungleInterstitialAd(properties)`
*   Kotlin/Java: `VungleInterstitialAd.with(context, "ID")`

**Rewarded:**
*   Compose: `rememberVungleRewardedAd(properties)`
*   Kotlin/Java: `VungleRewardedAd.with(context, "ID")`

**Banner:**
*   Compose: `VungleBannerAd(properties)`
*   Kotlin/Java: `VungleBannerView(context)`

---

### 6. InMobi Module (`inmobi`)
**Setup Requirement:** Requires SDK initialization.
**Init (Compose):** `val isInitialized = rememberInMobiAdsInit("ACCOUNT_ID")`
**Init (Kotlin/Java):** `InMobiAds.init(context, "ACCOUNT_ID", callback)`

#### Formats Implementation
**Interstitial:**
*   Compose: `rememberInMobiInterstitialAd(properties)`
*   Kotlin/Java: `InMobiInterstitialAd.with(context, 123456789L)` // Uses Long IDs

**Rewarded:**
*   Compose: `rememberInMobiRewardedAd(properties)`
*   Kotlin/Java: `InMobiRewardedAd.with(context, 123456789L)`

**Banner:**
*   Compose: `InMobiBannerAd(properties)`
*   Kotlin/Java: `InMobiBannerView(context)`

---

### 7. IronSource Module (`is`)
**Setup Requirement:** Requires SDK initialization.
**Init (Compose):** `val isInitialized = rememberIronSourceAdsInit("APP_KEY")`
**Init (Kotlin/Java):** `IronSourceAds.init(context, "APP_KEY", callback)`

#### Formats Implementation
**Interstitial:**
*   Compose: `rememberIronSourceInterstitialAd(properties)`
*   Kotlin/Java: `IronSourceInterstitialAd.with(context, "ID")`

**Rewarded:**
*   Compose: `rememberIronSourceRewardedAd(properties)`
*   Kotlin/Java: `IronSourceRewardedAd.with(context, "ID")`

**Banner:**
*   Compose: `IronSourceBannerAd(properties)`
*   Kotlin/Java: `IronSourceBannerView(context)`

---

## 📜 License

This project is licensed under the **MIT License**. See the [LICENSE](LICENSE) file for details.
