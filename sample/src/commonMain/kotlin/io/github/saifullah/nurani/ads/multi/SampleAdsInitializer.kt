package io.github.saifullah.nurani.ads.multi

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import io.github.saifullah.nurani.ads.admob.AdmobAds
import io.github.saifullah.nurani.ads.applovin.AppLovinAds
import io.github.saifullah.nurani.ads.core.compose.LocalPlatformContext
import io.github.saifullah.nurani.ads.core.compose.PlatformContext
import io.github.saifullah.nurani.ads.inmobi.InMobiAds
import io.github.saifullah.nurani.ads.man.MetaAds
import io.github.saifullah.nurani.ads.pangle.PangleAds
import io.github.saifullah.nurani.ads.ironsource.IronSourceAds
import io.github.saifullah.nurani.ads.vungle.VungleAds

data class SampleAdsKeys(
    val androidAppId: String,
    val iosAppId: String,
    val androidAppKey: String,
    val iosAppKey: String,
    val androidAccountId: String,
    val iosAccountId: String,
    val androidPangleAppId: String,
    val iosPangleAppId: String,
    val androidVungleAppId: String,
    val iosVungleAppId: String,
    val androidIronSourceAppKey: String,
    val iosIronSourceAppKey: String
)

@Composable
fun SampleAdsInitializer(
    keys: SampleAdsKeys = SampleAdsKeys(
        androidAppId = "ca-app-pub-3940256099942544~3347511713",
        iosAppId = "ca-app-pub-3940256099942544~1458002511",
        androidAppKey = "sample",
        iosAppKey = "sample",
        androidAccountId = "88010002dc5c45d48ba3c5e599d79136",
        iosAccountId = "88010002dc5c45d48ba3c5e599d79136",
        androidPangleAppId = "8817707",
        iosPangleAppId = "8025677",
        androidVungleAppId = "69ece8934bef11f475504020",
        iosVungleAppId = "6a2467f6e25cb91cc0d71511",
        androidIronSourceAppKey = "2636d0095",
        iosIronSourceAppKey = "2636d3a1d"
    ),
    context: PlatformContext = LocalPlatformContext.current
) {
    val isIos = platform().lowercase().contains("ios")

    LaunchedEffect(context, keys, isIos) {
        AdmobAds.init(context)
        MetaAds.init(
            context = context,
            androidPlacementId = "IMG_16_9_APP_INSTALL#YOUR_PLACEMENT_ID",
            iosPlacementId = "IMG_16_9_APP_INSTALL#YOUR_PLACEMENT_ID",
            onComplete = null
        )
        AppLovinAds.init(
            context = context,
            androidSdkKey = keys.androidAppKey,
            iosSdkKey = keys.iosAppKey,
            onComplete = null
        )
        InMobiAds.init(
            context = context,
            androidAccountId = keys.androidAccountId,
            iosAccountId = keys.iosAccountId,
            onComplete = null
        )
        PangleAds.init(
            context = context,
            androidAppId = keys.androidPangleAppId,
            iosAppId = keys.iosPangleAppId,
            onComplete = null
        )
        VungleAds.init(
            context = context,
            androidAppId = keys.androidVungleAppId,
            iosAppId = keys.iosVungleAppId,
            onComplete = null
        )
        IronSourceAds.init(
            context = context,
            androidAppKey = keys.androidIronSourceAppKey,
            iosAppKey = keys.iosIronSourceAppKey,
            onComplete = null
        )
    }
}
