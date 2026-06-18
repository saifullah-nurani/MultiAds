package io.github.saifullah.nurani.ads.multi

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.saifullah.nurani.ads.core.AdSize
import io.github.saifullah.nurani.ads.core.BannerAd
import io.github.saifullah.nurani.ads.core.compose.LocalPlatformActivity

// Meta (MAN) Imports
import io.github.saifullah.nurani.ads.man.compose.MetaBannerAd
import io.github.saifullah.nurani.ads.man.compose.metaPlacementProperties
import io.github.saifullah.nurani.ads.man.compose.rememberMetaInterstitialAd
import io.github.saifullah.nurani.ads.man.compose.rememberMetaRewardedAd

// AppLovin MAX Imports
import io.github.saifullah.nurani.ads.applovin.compose.AppLovinBannerAd
import io.github.saifullah.nurani.ads.applovin.compose.appLovinAdProperties
import io.github.saifullah.nurani.ads.applovin.compose.AppLovinAdProperties
import io.github.saifullah.nurani.ads.applovin.compose.rememberAppLovinInterstitialAd
import io.github.saifullah.nurani.ads.applovin.compose.rememberAppLovinRewardedAd

// InMobi Imports
import io.github.saifullah.nurani.ads.inmobi.compose.InMobiBannerAd
import io.github.saifullah.nurani.ads.inmobi.compose.inMobiPlacementProperties
import io.github.saifullah.nurani.ads.inmobi.compose.rememberInMobiInterstitialAd
import io.github.saifullah.nurani.ads.inmobi.compose.rememberInMobiRewardedAd
import io.github.saifullah.nurani.ads.inmobi.compose.rememberInMobiAdsInit

// Vungle Imports
import io.github.saifullah.nurani.ads.vungle.compose.VungleBannerAd
import io.github.saifullah.nurani.ads.vungle.compose.vunglePlacementProperties
import io.github.saifullah.nurani.ads.vungle.compose.rememberVungleInterstitialAd
import io.github.saifullah.nurani.ads.vungle.compose.rememberVungleRewardedAd
import io.github.saifullah.nurani.ads.vungle.compose.rememberVungleAdsInit

// Pangle Imports
import io.github.saifullah.nurani.ads.pangle.compose.PangleBannerAd
import io.github.saifullah.nurani.ads.pangle.compose.pangleAdProperties
import io.github.saifullah.nurani.ads.pangle.compose.PangleAdProperties
import io.github.saifullah.nurani.ads.pangle.compose.rememberPangleInterstitialAd
import io.github.saifullah.nurani.ads.pangle.compose.rememberPangleRewardedAd
import io.github.saifullah.nurani.ads.pangle.compose.rememberPangleAdsInit

// IronSource Imports
import io.github.saifullah.nurani.ads.`is`.compose.IronSourceBannerAd
import io.github.saifullah.nurani.ads.`is`.compose.ironSourceAdProperties
import io.github.saifullah.nurani.ads.`is`.compose.rememberIronSourceInterstitialAd
import io.github.saifullah.nurani.ads.`is`.compose.rememberIronSourceRewardedAd
import io.github.saifullah.nurani.ads.`is`.compose.rememberIronSourceAdsInit

@Composable
fun PlatformSpecificScreens(screen: String, onBack: () -> Unit): Boolean {
    return when (screen) {
        "MultiAdsWaterfall" -> {
            MultiAdsWaterfallScreen(onBack)
            true
        }

        "MetaTest" -> {
            MetaTestScreen(onBack)
            true
        }

        "AppLovinTest" -> {
            AppLovinTestScreen(onBack)
            true
        }

        "InMobiTest" -> {
            InMobiTestScreen(onBack)
            true
        }

        "VungleTest" -> {
            VungleTestScreen(onBack)
            true
        }

        "PangleTest" -> {
            PangleTestScreen(onBack)
            true
        }

        "IronSourceTest" -> {
            IronSourceTestScreen(onBack)
            true
        }

        else -> false
    }
}

// ==========================================
// 1. Meta (Audience Network) Test Screen
// ==========================================
@Composable
fun MetaTestScreen(onBack: () -> Unit) {
    val activity = LocalPlatformActivity.current
    val scrollState = rememberScrollState()

    val metaProperties = metaPlacementProperties(
        androidPlacementId = "IMG_16_9_APP_INSTALL#YOUR_PLACEMENT_ID",
        iosPlacementId = "IMG_16_9_APP_INSTALL#YOUR_PLACEMENT_ID"
    )

    val metaRewardedProperties = metaPlacementProperties(
        androidPlacementId = "VID_HD_16_9_46S_APP_INSTALL#YOUR_PLACEMENT_ID",
        iosPlacementId = "VID_HD_16_9_46S_APP_INSTALL#YOUR_PLACEMENT_ID"
    )

    val interstitialAd = rememberMetaInterstitialAd(
        properties = metaProperties,
        testModeEnabled = false
    )

    val rewardedAd = rememberMetaRewardedAd(
        properties = metaRewardedProperties,
        testModeEnabled = false
    )

    Scaffold(
        topBar = {
            TopAppBarRow(title = "Meta (MAN) Showcase", onBack = onBack)
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Banner Section
            ShowcaseCard(title = "Meta Banner Ad") {
                MetaBannerAd(
                    properties = metaProperties,
                    adSize = BannerAd.Fixed(AdSize.BANNER),
                    testModeEnabled = true,
                    expandWhenReady = true
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Fullscreen Ads Section
            ShowcaseCard(title = "Meta Full Screen Ads") {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    AdControlSection(
                        title = "Meta Interstitial",
                        ad = interstitialAd,
                        onShow = { if (activity != null) interstitialAd.showAd(activity) },
                        onLoad = { interstitialAd.loadAd() }
                    )

                    AdControlSection(
                        title = "Meta Rewarded",
                        ad = rewardedAd,
                        onShow = { if (activity != null) rewardedAd.showAd(activity) },
                        onLoad = { rewardedAd.loadAd() }
                    )
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// ==========================================
// 2. AppLovin (MAX) Test Screen
// ==========================================
@Composable
fun AppLovinTestScreen(onBack: () -> Unit) {
    val activity = LocalPlatformActivity.current
    val scrollState = rememberScrollState()

    val appLovinProperties = appLovinAdProperties(
        androidAdUnitId = "YOUR_MAX_AD_UNIT_ID",
        iosAdUnitId = "YOUR_MAX_AD_UNIT_ID"
    )

    val interstitialAd = rememberAppLovinInterstitialAd(
        properties = appLovinProperties,
        testModeEnabled = false
    )

    val rewardedAd = rememberAppLovinRewardedAd(
        properties = appLovinProperties,
        testModeEnabled = false
    )

    Scaffold(
        topBar = {
            TopAppBarRow(title = "AppLovin MAX Showcase", onBack = onBack)
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Banner Section
            ShowcaseCard(title = "MAX Banner Ad") {
                AppLovinBannerAd(
                    properties = appLovinProperties,
                    adSize = BannerAd.Fixed(AdSize.BANNER),
                    testModeEnabled = true,
                    expandWhenReady = true
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Fullscreen Ads Section
            ShowcaseCard(title = "MAX Full Screen Ads") {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    AdControlSection(
                        title = "MAX Interstitial",
                        ad = interstitialAd,
                        onShow = { if (activity != null) interstitialAd.showAd(activity) },
                        onLoad = { interstitialAd.loadAd() }
                    )

                    AdControlSection(
                        title = "MAX Rewarded",
                        ad = rewardedAd,
                        onShow = { if (activity != null) rewardedAd.showAd(activity) },
                        onLoad = { rewardedAd.loadAd() }
                    )
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// ==========================================
// 3. InMobi Test Screen
// ==========================================
@Composable
fun InMobiTestScreen(onBack: () -> Unit) {
    val isInitialized = rememberInMobiAdsInit(
        androidAccountId = "88010002dc5c45d48ba3c5e599d79136",
        iosAccountId = "88010002dc5c45d48ba3c5e599d79136"
    )

    if (!isInitialized) {
        Scaffold(
            topBar = {
                TopAppBarRow(title = "InMobi Showcase", onBack = onBack)
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        return
    }

    val activity = LocalPlatformActivity.current
    val scrollState = rememberScrollState()

    val inmobiProperties = inMobiPlacementProperties(
        androidPlacementId = 10000718284,
        iosPlacementId = 10000718551
    )

    val inmobiInterstitialProperties = inMobiPlacementProperties(
        androidPlacementId = 10000718282,
        iosPlacementId = 10000718549
    )

    val inmobiRewardedProperties = inMobiPlacementProperties(
        androidPlacementId = 10000718283,
        iosPlacementId = 10000718552
    )

    val interstitialAd = rememberInMobiInterstitialAd(
        properties = inmobiInterstitialProperties,
        testModeEnabled = false
    )

    val rewardedAd = rememberInMobiRewardedAd(
        properties = inmobiRewardedProperties,
        testModeEnabled = false
    )

    Scaffold(
        topBar = {
            TopAppBarRow(title = "InMobi Showcase", onBack = onBack)
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Banner Section
            ShowcaseCard(title = "InMobi Banner Ad") {
                InMobiBannerAd(
                    properties = inmobiProperties,
                    adSize = BannerAd.Fixed(AdSize.BANNER),
                    testModeEnabled = true,
                    expandWhenReady = true
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Fullscreen Ads Section
            ShowcaseCard(title = "InMobi Full Screen Ads") {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    AdControlSection(
                        title = "InMobi Interstitial",
                        ad = interstitialAd,
                        onShow = { if (activity != null) interstitialAd.showAd(activity) },
                        onLoad = { interstitialAd.loadAd() }
                    )

                    AdControlSection(
                        title = "InMobi Rewarded",
                        ad = rewardedAd,
                        onShow = { if (activity != null) rewardedAd.showAd(activity) },
                        onLoad = { rewardedAd.loadAd() }
                    )
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// ==========================================
// 4. Vungle Test Screen
// ==========================================
@Composable
fun VungleTestScreen(onBack: () -> Unit) {
    val isInitialized = rememberVungleAdsInit(
        androidAppId = "69ece8934bef11f475504020",
        iosAppId = "6a2467f6e25cb91cc0d71511"
    )

    if (!isInitialized) {
        Scaffold(
            topBar = {
                TopAppBarRow(title = "Vungle Showcase", onBack = onBack)
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        return
    }

    val activity = LocalPlatformActivity.current
    val scrollState = rememberScrollState()

    val vungleProperties = vunglePlacementProperties(
        androidPlacementId = "B1-5606155",
        iosPlacementId = "B1-5106071"
    )

    val vungleInterstitialProperties = vunglePlacementProperties(
        androidPlacementId = "INTERSTITIAL-1491904",
        iosPlacementId = "I1-8348515"
    )

    val vungleRewardedProperties = vunglePlacementProperties(
        androidPlacementId = "R1-9273153",
        iosPlacementId = "R1-5035381"
    )

    val interstitialAd = rememberVungleInterstitialAd(
        properties = vungleInterstitialProperties,
        testModeEnabled = false
    )

    val rewardedAd = rememberVungleRewardedAd(
        properties = vungleRewardedProperties,
        testModeEnabled = false
    )

    Scaffold(
        topBar = {
            TopAppBarRow(title = "Vungle Showcase", onBack = onBack)
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Banner Section
            ShowcaseCard(title = "Vungle Banner Ad") {
                VungleBannerAd(
                    properties = vungleProperties,
                    adSize = BannerAd.Fixed(AdSize.BANNER),
                    testModeEnabled = true,
                    expandWhenReady = true
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Fullscreen Ads Section
            ShowcaseCard(title = "Vungle Full Screen Ads") {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    AdControlSection(
                        title = "Vungle Interstitial",
                        ad = interstitialAd,
                        onShow = { if (activity != null) interstitialAd.showAd(activity) },
                        onLoad = { interstitialAd.loadAd() }
                    )

                    AdControlSection(
                        title = "Vungle Rewarded",
                        ad = rewardedAd,
                        onShow = { if (activity != null) rewardedAd.showAd(activity) },
                        onLoad = { rewardedAd.loadAd() }
                    )
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// ==========================================
// 5. Pangle Showcase Test Screen
// ==========================================
@Composable
fun PangleTestScreen(onBack: () -> Unit) {
    val isInitialized = rememberPangleAdsInit(
        androidAppId = "8817707",
        iosAppId = "8025677"
    )

    if (!isInitialized) {
        Scaffold(
            topBar = {
                TopAppBarRow(title = "Pangle Showcase", onBack = onBack)
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        return
    }

    val activity = LocalPlatformActivity.current
    val scrollState = rememberScrollState()

    val pangleProperties = pangleAdProperties(
        androidAdUnitId = "983238489",
        iosAdUnitId = "980099802"
    )

    val pangleInterstitialProperties = pangleAdProperties(
        androidAdUnitId = "983238463",
        iosAdUnitId = "980088188"
    )

    val pangleRewardedProperties = pangleAdProperties(
        androidAdUnitId = "983067077",
        iosAdUnitId = "980088192"
    )

    val interstitialAd = rememberPangleInterstitialAd(
        properties = pangleInterstitialProperties,
        testModeEnabled = false
    )

    val rewardedAd = rememberPangleRewardedAd(
        properties = pangleRewardedProperties,
        testModeEnabled = false
    )

    Scaffold(
        topBar = {
            TopAppBarRow(title = "Pangle Showcase", onBack = onBack)
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Banner Section
            ShowcaseCard(title = "Pangle Banner Ad") {
                PangleBannerAd(
                    properties = pangleProperties,
                    adSize = BannerAd.Fixed(AdSize.BANNER),
                    testModeEnabled = true,
                    expandWhenReady = true
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Fullscreen Ads Section
            ShowcaseCard(title = "Pangle Full Screen Ads") {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    AdControlSection(
                        title = "Pangle Interstitial",
                        ad = interstitialAd,
                        onShow = { if (activity != null) interstitialAd.showAd(activity) },
                        onLoad = { interstitialAd.loadAd() }
                    )

                    AdControlSection(
                        title = "Pangle Rewarded",
                        ad = rewardedAd,
                        onShow = { if (activity != null) rewardedAd.showAd(activity) },
                        onLoad = { rewardedAd.loadAd() }
                    )
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// ==========================================
// 6. IronSource (LevelPlay) Showcase Test Screen
// ==========================================
@Composable
fun IronSourceTestScreen(onBack: () -> Unit) {
    val isInitialized = rememberIronSourceAdsInit(
        androidAppKey = "2636d0095",
        iosAppKey = "2636d3a1d",
        testModeEnabled = true
    )
    if (!isInitialized) {
        Scaffold(
            topBar = {
                TopAppBarRow(title = "IronSource Showcase", onBack = onBack)
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        return
    }

    val activity = LocalPlatformActivity.current
    val scrollState = rememberScrollState()

    val ironsourceProperties = ironSourceAdProperties(
        androidPlacementName = "ll7laet2x8ilqdee",
        iosPlacementName = "ch132493tceqkqsg"
    )

    val ironsourceInterstitialProperties = ironSourceAdProperties(
        androidPlacementName = "i51skyerg3iiyyaq",
        iosPlacementName = "re3gip7b41tqb2tm"
    )

    val ironsourceRewardedProperties = ironSourceAdProperties(
        androidPlacementName = "2452nmjt1t4g9z33",
        iosPlacementName = "1hv15us4p1j74q7j"
    )

    val interstitialAd = rememberIronSourceInterstitialAd(
        properties = ironsourceInterstitialProperties,
        testModeEnabled = true
    )

    val rewardedAd = rememberIronSourceRewardedAd(
        properties = ironsourceRewardedProperties,
        testModeEnabled = true
    )

    Scaffold(
        topBar = {
            TopAppBarRow(title = "IronSource Showcase", onBack = onBack)
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Banner Section
            ShowcaseCard(title = "IronSource Banner Ad") {
                IronSourceBannerAd(
                    properties = ironsourceProperties,
                    adSize = BannerAd.Fixed(AdSize.BANNER),
                    testModeEnabled = true,
                    expandWhenReady = true
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Fullscreen Ads Section
            ShowcaseCard(title = "IronSource Full Screen Ads") {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    AdControlSection(
                        title = "IronSource Interstitial",
                        ad = interstitialAd,
                        onShow = { if (activity != null) interstitialAd.showAd(activity) },
                        onLoad = { interstitialAd.loadAd() }
                    )

                    AdControlSection(
                        title = "IronSource Rewarded",
                        ad = rewardedAd,
                        onShow = { if (activity != null) rewardedAd.showAd(activity) },
                        onLoad = { rewardedAd.loadAd() }
                    )
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// ==========================================
// Helper UI Components
// ==========================================
@Composable
private fun TopAppBarRow(title: String, onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(WindowInsets.statusBars.asPaddingValues())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedButton(
            onClick = onBack,
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Back")
        }
        Text(
            text = title,
            modifier = Modifier.padding(start = 16.dp),
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )
        )
    }
}

@Composable
private fun ShowcaseCard(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}
