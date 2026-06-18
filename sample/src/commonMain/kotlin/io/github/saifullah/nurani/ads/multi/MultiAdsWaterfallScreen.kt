package io.github.saifullah.nurani.ads.multi

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.saifullah.nurani.ads.core.AdSize
import io.github.saifullah.nurani.ads.core.BannerAd
import io.github.saifullah.nurani.ads.core.BannerAdListener
import io.github.saifullah.nurani.ads.core.AdLoadCallback
import io.github.saifullah.nurani.ads.core.AdContentCallback
import io.github.saifullah.nurani.ads.multi.compose.MultiBannerAd
import io.github.saifullah.nurani.ads.multi.compose.rememberMultiInterstitialAd
import io.github.saifullah.nurani.ads.multi.compose.rememberMultiRewardedAd
import io.github.saifullah.nurani.ads.multi.compose.rememberMultiAppOpenAd
import io.github.saifullah.nurani.ads.multi.models.AdNetwork
import io.github.saifullah.nurani.ads.multi.models.WaterfallConfig
import io.github.saifullah.nurani.ads.multi.models.waterfallConfig
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiAdsWaterfallScreen(onBack: () -> Unit) {
    val scrollState = rememberScrollState()
    val isIos = platform().lowercase().contains("ios")

    // Test Placements Configuration
    val admobBannerId = if (isIos) "ca-app-pub-3940256099942544/2934735716" else "ca-app-pub-3940256099942544/6300978111"
    val admobInterstitialId = if (isIos) "ca-app-pub-3940256099942544/4411468910" else "ca-app-pub-3940256099942544/1033173712"
    val admobRewardedId = if (isIos) "ca-app-pub-3940256099942544/1712485313" else "ca-app-pub-3940256099942544/5224354917"
    val admobAppOpenId = if (isIos) "ca-app-pub-3940256099942544/5575461041" else "ca-app-pub-3940256099942544/9257395921"

    val pangleBannerId = if (isIos) "983240210" else "983238454"
    val pangleInterstitialId = if (isIos) "980088188" else "983238463"
    val pangleRewardedId = if (isIos) "980088192" else "983067077"

    val inmobiBannerId = if (isIos) 10000718551L else 10000718284L
    val inmobiInterstitialId = if (isIos) 10000718549L else 10000718282L
    val inmobiRewardedId = if (isIos) 10000718552L else 10000718283L

    val ironSourceBannerId = if (isIos) "24965124" else "ch132493tceqkqsg"
    val ironSourceInterstitialId = if (isIos) "re3gip7b41tqb2tm" else "i51skyerg3iiyyaq"
    val ironSourceRewardedId = if (isIos) "1hv15us4p1j74q7j" else "2452nmjt1t4g9z33"

    val vungleBannerId = if (isIos) "B1-5106071" else "B1-5606155"
    val vungleInterstitialId = if (isIos) "I1-8348515" else "INTERSTITIAL-1491904"
    val vungleRewardedId = if (isIos) "R1-5035381" else "R1-9273153"

    val metaBannerId = "IMG_16_9_APP_INSTALL#YOUR_PLACEMENT_ID"
    val metaInterstitialId = "IMG_16_9_APP_INSTALL#YOUR_PLACEMENT_ID"
    val metaRewardedId = "VID_HD_16_9_46S_APP_INSTALL#YOUR_PLACEMENT_ID"

    val applovinAdUnitId = "YOUR_MAX_AD_UNIT_ID"

    // Configuration States
    var primaryNetwork by remember { mutableStateOf(AdNetwork.ADMOB) }
    var selectedAdSizeName by remember { mutableStateOf("BANNER") }
    var maxConcurrentLoads by remember { mutableStateOf(2) }

    var loadKey by remember { mutableStateOf(0) }
    val logs = remember { mutableStateListOf<String>() }

    fun logEvent(tag: String, message: String) {
        logs.add(0, "[$tag] $message")
        if (logs.size > 50) {
            logs.removeLast()
        }
    }

    // Map selected size name to actual AdSize
    val selectedAdSize = remember(selectedAdSizeName) {
        when (selectedAdSizeName) {
            "BANNER" -> BannerAd.Fixed(AdSize.BANNER)
            "LARGE_BANNER" -> BannerAd.Fixed(AdSize.LARGE_BANNER)
            "MEDIUM_RECTANGLE" -> BannerAd.Fixed(AdSize.MEDIUM_RECTANGLE)
            else -> BannerAd.Fixed(AdSize.BANNER)
        }
    }

    // Helper to dynamically build WaterfallConfig
    fun buildWaterfall(format: String): WaterfallConfig {
        return waterfallConfig {
            maxConcurrentLoads(maxConcurrentLoads)
            val networks = listOf(
                AdNetwork.ADMOB,
                AdNetwork.APPLOVIN,
                AdNetwork.META,
                AdNetwork.VUNGLE,
                AdNetwork.INMOBI,
                AdNetwork.PANGLE,
                AdNetwork.IRONSOURCE
            ).toMutableList()
            networks.remove(primaryNetwork)
            networks.add(0, primaryNetwork) // Set chosen network as priority 1

            networks.forEachIndexed { index, network ->
                val priority = index + 1
                when (network) {
                    AdNetwork.ADMOB -> {
                        val adUnitId = when (format) {
                            "banner" -> admobBannerId
                            "interstitial" -> admobInterstitialId
                            "appopen" -> admobAppOpenId
                            else -> admobRewardedId
                        }
                        admob(adUnitId = adUnitId, priority = priority)
                    }
                    AdNetwork.PANGLE -> {
                        val adUnitId = when (format) {
                            "banner" -> pangleBannerId
                            "interstitial" -> pangleInterstitialId
                            else -> pangleRewardedId
                        }
                        pangle(adUnitId = adUnitId, priority = priority)
                    }
                    AdNetwork.INMOBI -> {
                        val placementId = when (format) {
                            "banner" -> inmobiBannerId
                            "interstitial" -> inmobiInterstitialId
                            else -> inmobiRewardedId
                        }
                        inmobi(placementId = placementId, priority = priority)
                    }
                    AdNetwork.IRONSOURCE -> {
                        val placementName = when (format) {
                            "banner" -> ironSourceBannerId
                            "interstitial" -> ironSourceInterstitialId
                            else -> ironSourceRewardedId
                        }
                        ironsource(placementName = placementName, priority = priority)
                    }
                    AdNetwork.VUNGLE -> {
                        val placementId = when (format) {
                            "banner" -> vungleBannerId
                            "interstitial" -> vungleInterstitialId
                            else -> vungleRewardedId
                        }
                        vungle(placementId = placementId, priority = priority)
                    }
                    AdNetwork.META -> {
                        val placementId = when (format) {
                            "banner" -> metaBannerId
                            "interstitial" -> metaInterstitialId
                            else -> metaRewardedId
                        }
                        meta(placementId = placementId, priority = priority)
                    }
                    AdNetwork.APPLOVIN -> {
                        applovin(adUnitId = applovinAdUnitId, priority = priority)
                    }
                }
            }
        }
    }

    // Generate configs dynamically based on settings
    val waterfallBanner = remember(primaryNetwork, maxConcurrentLoads) {
        buildWaterfall("banner")
    }

    val waterfallInterstitial = remember(primaryNetwork, maxConcurrentLoads) {
        buildWaterfall("interstitial")
    }

    val waterfallRewarded = remember(primaryNetwork, maxConcurrentLoads) {
        buildWaterfall("rewarded")
    }

    val waterfallAppOpen = remember(primaryNetwork, maxConcurrentLoads) {
        buildWaterfall("appopen")
    }

    // Callbacks listeners
    val bannerListener = remember {
        BannerAdListener(
            onAdLoaded = { logEvent("Banner", "Waterfall Loaded successfully") },
            onAdFailedToLoad = { error -> logEvent("Banner", "Waterfall Failed: ${error?.message ?: "Unknown"}") },
            onAdClicked = { logEvent("Banner", "Clicked") },
            onAdDisplayed = { logEvent("Banner", "Displayed") },
            onAdDismissed = { logEvent("Banner", "Dismissed") }
        )
    }

    val interstitialLoadCallback = remember {
        AdLoadCallback(
            onAdLoaded = { logEvent("Interstitial", "Waterfall ad loaded successfully and ready to show") },
            onAdFailedToLoad = { error -> logEvent("Interstitial", "Waterfall ad failed: ${error?.message ?: "Unknown"}") }
        )
    }

    val interstitialContentCallback = remember {
        AdContentCallback(
            onAdShowed = { logEvent("Interstitial", "Showed") },
            onAdDisplayed = { logEvent("Interstitial", "Fully displayed") },
            onAdDismissed = { logEvent("Interstitial", "Dismissed") },
            onAdClicked = { logEvent("Interstitial", "Clicked") },
            onAdFailedToShow = { error -> logEvent("Interstitial", "Failed to show: ${error?.message ?: "Unknown"}") }
        )
    }

    val rewardedLoadCallback = remember {
        AdLoadCallback(
            onAdLoaded = { logEvent("Rewarded", "Waterfall ad loaded successfully and ready to show") },
            onAdFailedToLoad = { error -> logEvent("Rewarded", "Waterfall ad failed: ${error?.message ?: "Unknown"}") }
        )
    }

    val rewardedContentCallback = remember {
        AdContentCallback(
            onAdShowed = { logEvent("Rewarded", "Showed") },
            onAdDisplayed = { logEvent("Rewarded", "Fully displayed") },
            onAdDismissed = { logEvent("Rewarded", "Dismissed") },
            onAdClicked = { logEvent("Rewarded", "Clicked") },
            onAdFailedToShow = { error -> logEvent("Rewarded", "Failed to show: ${error?.message ?: "Unknown"}") }
        )
    }

    val onUserRewarded = remember {
        {
            logEvent("Rewarded", "User rewarded successfully!")
        }
    }

    val activity = io.github.saifullah.nurani.ads.core.compose.LocalPlatformActivity.current

    val interstitialAd = rememberMultiInterstitialAd(
        waterfallConfig = waterfallInterstitial,
        testModeEnabled = true,
        initialLoad = false,
        adLoadCallback = interstitialLoadCallback,
        adContentCallback = interstitialContentCallback
    )

    val rewardedAd = rememberMultiRewardedAd(
        waterfallConfig = waterfallRewarded,
        testModeEnabled = true,
        initialLoad = false,
        adLoadCallback = rewardedLoadCallback,
        adContentCallback = rewardedContentCallback,
        onUserRewarded = onUserRewarded
    )

    val appOpenAdLoadCallback = remember {
        AdLoadCallback(
            onAdLoaded = { logEvent("AppOpen", "Waterfall ad loaded successfully and ready to show") },
            onAdFailedToLoad = { error -> logEvent("AppOpen", "Waterfall ad failed: ${error?.message ?: "Unknown"}") }
        )
    }

    val appOpenAdContentCallback = remember {
        AdContentCallback(
            onAdShowed = { logEvent("AppOpen", "Showed") },
            onAdDisplayed = { logEvent("AppOpen", "Fully displayed") },
            onAdDismissed = { logEvent("AppOpen", "Dismissed") },
            onAdClicked = { logEvent("AppOpen", "Clicked") },
            onAdFailedToShow = { error -> logEvent("AppOpen", "Failed to show: ${error?.message ?: "Unknown"}") }
        )
    }

    val appOpenAd = rememberMultiAppOpenAd(
        waterfallConfig = waterfallAppOpen,
        testModeEnabled = true,
        initialLoad = false,
        adLoadCallback = appOpenAdLoadCallback,
        adContentCallback = appOpenAdContentCallback
    )

    Scaffold(
        topBar = {
            WaterfallTopAppBarRow(title = "Waterfall Mediation", onBack = onBack)
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f),
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            WaterfallInfoCard(
                title = "Priority Waterfall Mediation Test Screen",
                body = "Configure priority, size, and concurrency parameters to test multi-network mediation logic. Fallback occurs automatically on failure."
            )

            // Dynamic Configurations Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "1. Configure Waterfall Settings",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    // Priority Network Selector
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Select Primary Priority (Priority 1):",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val availableNetworks = listOf(
                                AdNetwork.ADMOB,
                                AdNetwork.APPLOVIN,
                                AdNetwork.META,
                                AdNetwork.VUNGLE,
                                AdNetwork.INMOBI,
                                AdNetwork.PANGLE,
                                AdNetwork.IRONSOURCE
                            )
                            items(availableNetworks) { network ->
                                val label = network.name.lowercase().replaceFirstChar { it.uppercase() }
                                FilterChip(
                                    selected = primaryNetwork == network,
                                    onClick = {
                                        primaryNetwork = network
                                        logEvent("Config", "Primary priority updated to: ${network.name}")
                                        loadKey++ // Force reload banner
                                    },
                                    label = { Text(label) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                )
                            }
                        }
                    }

                    HorizontalDivider()

                    // Banner Ad Size Selector
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Banner Size Strategy:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf("BANNER", "LARGE_BANNER", "MEDIUM_RECTANGLE").forEach { sizeName ->
                                OutlinedButton(
                                    onClick = {
                                        selectedAdSizeName = sizeName
                                        logEvent("Config", "Banner size updated to: $sizeName")
                                        loadKey++
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = if (selectedAdSizeName == sizeName) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                                    ),
                                    border = ButtonDefaults.outlinedButtonBorder(true)
                                ) {
                                    Text(
                                        text = sizeName.replace("_", " "),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }

                    HorizontalDivider()

                    // Concurrency Selector
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Max Concurrent Ad Loads: $maxConcurrentLoads",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf(1, 2, 3).forEach { concurrency ->
                                OutlinedButton(
                                    onClick = {
                                        maxConcurrentLoads = concurrency
                                        logEvent("Config", "Max concurrent loads updated to: $concurrency")
                                        loadKey++
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = if (maxConcurrentLoads == concurrency) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                                    )
                                ) {
                                    Text(text = "$concurrency Load(s)", fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }
            }

            // Banner Ad Format Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "2. Unified Banner Waterfall",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = { loadKey++ }) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Reload Banner"
                            )
                        }
                    }

                    Text(
                        text = "Mediation starting with: ${primaryNetwork.name}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        key(loadKey) {
                            MultiBannerAd(
                                waterfallConfig = waterfallBanner,
                                testModeEnabled = true,
                                adSize = selectedAdSize,
                                adListener = bannerListener
                            )
                        }
                    }
                }
            }

            // Full Screen Ads Format Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "3. Unified Full Screen Waterfalls",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    AdControlSection(
                        title = "Waterfall Interstitial",
                        ad = interstitialAd,
                        onShow = { if (activity != null) interstitialAd.showAd(activity) },
                        onLoad = {
                            logEvent("Interstitial", "Start loading waterfall...")
                            interstitialAd.loadAd()
                        }
                    )

                    AdControlSection(
                        title = "Waterfall Rewarded",
                        ad = rewardedAd,
                        onShow = { if (activity != null) rewardedAd.showAd(activity) },
                        onLoad = {
                            logEvent("Rewarded", "Start loading waterfall...")
                            rewardedAd.loadAd()
                        }
                    )

                    AdControlSection(
                        title = "Waterfall App Open",
                        ad = appOpenAd,
                        onShow = { if (activity != null) appOpenAd.showAd(activity) },
                        onLoad = {
                            logEvent("AppOpen", "Start loading waterfall...")
                            appOpenAd.loadAd()
                        }
                    )

                    HorizontalDivider()

                    Text(
                        text = "App Open Lifecycle Trigger (Android)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                PlatformAdLifecycleHelper.registerTestTrigger(activity, appOpenAd) { msg ->
                                    logEvent("Trigger", msg)
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Register ON_START Trigger", fontSize = 11.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                PlatformAdLifecycleHelper.clearTriggers()
                                logEvent("Trigger", "All triggers cleared.")
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Clear Triggers", fontSize = 11.sp)
                        }
                    }
                }
            }

            // Real-time Mediation Log Console Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Waterfall Console Logs",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        TextButton(onClick = { logs.clear() }) {
                            Text("Clear")
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black)
                            .border(1.dp, Color.Gray.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        if (logs.isEmpty()) {
                            Text(
                                text = "Console is empty. Click actions above to trigger waterfall mediation.",
                                color = Color.Green.copy(alpha = 0.6f),
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                items(logs) { log ->
                                    Text(
                                        text = log,
                                        color = if (log.contains("Failed") || log.contains("failed")) Color.Red else if (log.contains("success") || log.contains("Rewarded")) Color.Green else Color.Cyan,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun WaterfallTopAppBarRow(title: String, onBack: () -> Unit) {
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
private fun WaterfallInfoCard(
    title: String,
    body: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
