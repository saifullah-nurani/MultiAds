package io.github.saifullah.nurani.ads.multi

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import io.github.saifullah.nurani.ads.admob.compose.admobAdProperties
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Icon
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.animation.core.InfiniteRepeatableSpec
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.width
import androidx.compose.ui.draw.alpha
import io.github.saifullah.nurani.ads.admob.compose.AdmobBannerAd
import io.github.saifullah.nurani.ads.admob.compose.rememberAdmobInterstitialAd
import io.github.saifullah.nurani.ads.admob.compose.rememberAdmobRewardedAd
import io.github.saifullah.nurani.ads.admob.compose.rememberAdmobRewardedInterstitialAd
import io.github.saifullah.nurani.ads.core.AdLogger
import io.github.saifullah.nurani.ads.core.AdSize
import io.github.saifullah.nurani.ads.admob.compose.AdmobAdProperties
import io.github.saifullah.nurani.ads.core.BannerAd
import io.github.saifullah.nurani.ads.core.AdState
import io.github.saifullah.nurani.ads.core.compose.LocalPlatformActivity
import io.github.saifullah.nurani.ads.core.compose.PlatformActivity
import io.github.saifullah.nurani.ads.core.utils.DefaultAdLogger

val PlaceHolderAdmobProperties = admobAdProperties("", "")

@Composable
fun AdmobTestScreen(onBack: () -> Unit) {
    var bannerType by remember { mutableStateOf("Fixed") }

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
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
                    text = "Admob Showcase",
                    modifier = Modifier.padding(start = 16.dp),
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
            }
        },
        bottomBar = {
            Spacer(modifier = Modifier.padding(WindowInsets.navigationBars.asPaddingValues()))
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .background(MaterialTheme.colorScheme.background)
        ) {
            BannerAdSection(
                bannerType = bannerType,
                onBannerTypeChange = { bannerType = it }
            )

            Spacer(modifier = Modifier.height(16.dp))
            FullScreenAdsSection()
            Spacer(modifier = Modifier.height(16.dp))
            AdConsoleCard()
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun BannerAdSection(bannerType: String, onBannerTypeChange: (String) -> Unit) {
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
                "Banner Ads",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = { onBannerTypeChange("Fixed") },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (bannerType == "Fixed") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Text("Fixed Size")
                }

                Button(
                    onClick = { onBannerTypeChange("Random") },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (bannerType == "Random") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Text("Random Size")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.LightGray.copy(alpha = 0.2f))
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                val bannerAdSize = if (bannerType == "Fixed") {
                    BannerAd.Fixed(AdSize.BANNER)
                } else {
                    BannerAd.Random(AdSize.LARGE_BANNER, AdSize.MEDIUM_RECTANGLE, AdSize.MEDIUM_RECTANGLE)
                }

                AdmobBannerAd(
                    properties = PlaceHolderAdmobProperties,
                    adSize = bannerAdSize,
                    testModeEnabled = true, // Force test mode for sample
                    adLogger = null,
                    adListener = createBannerListener("AdMob")
                )
            }
        }
    }
}

@Composable
fun FullScreenAdsSection() {

    val activity = LocalPlatformActivity.current

    // Note: Test Ad Unit IDs
    val interstitialAd = rememberAdmobInterstitialAd(
        properties = PlaceHolderAdmobProperties,
        testModeEnabled = true,
        adLogger = DefaultAdLogger(),
        adLoadCallback = createLoadCallback("AdMob", "Interstitial"),
        adContentCallback = createContentCallback("AdMob", "Interstitial")
    )

    val rewardedAd = rememberAdmobRewardedAd(
        properties = PlaceHolderAdmobProperties,
        testModeEnabled = true,
        adLogger = DefaultAdLogger(),
        adLoadCallback = createLoadCallback("AdMob", "Rewarded"),
        adContentCallback = createContentCallback("AdMob", "Rewarded")
    )

    val rewardedInterstitialAd = rememberAdmobRewardedInterstitialAd(
        properties = PlaceHolderAdmobProperties,
        testModeEnabled = true,
        adLogger = DefaultAdLogger(),
        adLoadCallback = createLoadCallback("AdMob", "RewardedInterstitial"),
        adContentCallback = createContentCallback("AdMob", "RewardedInterstitial")
    )

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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Full Screen Ads",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Start)
            )

            AdControlSection(
                title = "Interstitial",
                ad = interstitialAd,
                onShow = { if (activity != null) interstitialAd.showAd(activity) },
                onLoad = { interstitialAd.loadAd() }
            )

            AdControlSection(
                title = "Rewarded",
                ad = rewardedAd,
                onShow = { if (activity != null) rewardedAd.showAd(activity) },
                onLoad = { rewardedAd.loadAd() }
            )

            AdControlSection(
                title = "Rewarded Interstitial",
                ad = rewardedInterstitialAd,
                onShow = { if (activity != null) rewardedInterstitialAd.showAd(activity) },
                onLoad = { rewardedInterstitialAd.loadAd() }
            )
        }
    }
}

@Composable
fun AdControlSection(
    title: String,
    ad: AdState,
    onShow: () -> Unit,
    onLoad: () -> Unit
) {
    var tick by remember { mutableStateOf(0) }
    LaunchedEffect(ad) {
        while (true) {
            delay(250)
            tick++
        }
    }
    val currentTick = tick // Read the tick to register recomposition dependency

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
            AdStatusIndicator(ad)
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (ad.isAdAvailable) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onLoad,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Load $title")
                }
                Button(
                    onClick = onShow,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Show $title")
                }
            }
        } else {
            Button(
                onClick = onLoad,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                enabled = !ad.isAdLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Text(
                    if (ad.isAdLoading) "Loading..."
                    else "Load $title"
                )
            }
        }
    }
}

@Composable
fun AdStatusIndicator(ad: AdState) {
    val infiniteTransition = rememberInfiniteTransition()
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.4f,
        animationSpec = InfiniteRepeatableSpec(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        )
    )

    Row(verticalAlignment = Alignment.CenterVertically) {
        when {
            ad.isAdLoading -> {
                StatusBadge(
                    text = if (ad.isAdReloading) "Reloading" else if (ad.isAdRefreshing) "Refreshing" else "Loading",
                    color = MaterialTheme.colorScheme.primary,
                    icon = Icons.Default.Sync,
                    modifier = Modifier.alpha(pulseAlpha)
                )
            }
            ad.isRetryingAdFailedLoad -> {
                StatusBadge(
                    text = "Retrying ${ad.attemptCount}",
                    color = Color(0xFFF44336),
                    icon = Icons.Default.Refresh
                )
            }
            ad.isAdAvailable -> {
                StatusBadge(
                    text = "Ready",
                    color = Color(0xFF4CAF50),
                    icon = Icons.Default.CheckCircle
                )
            }
            else -> {
                StatusBadge(
                    text = "Idle",
                    color = Color.Gray,
                    icon = Icons.Default.Warning
                )
            }
        }
    }
}

@Composable
fun StatusBadge(
    text: String,
    color: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            color = color,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold
        )
    }
}
