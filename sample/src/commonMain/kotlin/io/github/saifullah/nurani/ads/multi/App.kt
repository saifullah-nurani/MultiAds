package io.github.saifullah.nurani.ads.multi

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun App() {

    MaterialTheme {
        var currentScreen by remember { mutableStateOf("Home") }

        if (currentScreen == "Home") {
            HomeScreen(
                onNavigateTo = { currentScreen = it }
            )
        } else if (currentScreen == "AdmobTest") {
            AdmobTestScreen(
                onBack = { currentScreen = "Home" }
            )
        } else {
            val handled = PlatformSpecificScreens(
                screen = currentScreen,
                onBack = { currentScreen = "Home" }
            )
            if (!handled) {
                currentScreen = "Home"
            }
        }
    }
}

@Composable
fun HomeScreen(onNavigateTo: (String) -> Unit) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
            .safeContentPadding()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // Hero Header
        Text(
            text = "MultiAds Showcase",
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Unified Cross-Platform Ad SDK Client Suite",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.secondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp, bottom = 32.dp)
        )

        // Ad Network Cards
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            NetworkNavigationCard(
                name = "Google AdMob",
                description = "Standard banner, interstitial, and rewarded placements.",
                color = Color(0xFF4CAF50),
                onClick = { onNavigateTo("AdmobTest") }
            )

            NetworkNavigationCard(
                name = "Multi-Ads Waterfall",
                description = "Priority-based loader with bounded concurrent loads and automatic fallback.",
                color = Color(0xFF7C4DFF),
                onClick = { onNavigateTo("MultiAdsWaterfall") }
            )

            NetworkNavigationCard(
                name = "Meta Audience Network",
                description = "High performing Meta/Facebook banner and full screen ads.",
                color = Color(0xFF1877F2),
                onClick = { onNavigateTo("MetaTest") }
            )

            NetworkNavigationCard(
                name = "AppLovin MAX",
                description = "Premium MAX mediation integration.",
                color = Color(0xFF0F1E36),
                onClick = { onNavigateTo("AppLovinTest") }
            )

            NetworkNavigationCard(
                name = "InMobi Ads",
                description = "Monetize with InMobi Banner and Interstitials.",
                color = Color(0xFFFF5722),
                onClick = { onNavigateTo("InMobiTest") }
            )

            NetworkNavigationCard(
                name = "Vungle (Liftoff)",
                description = "Vungle high-impact rewarded video ads & banners.",
                color = Color(0xFF00B0FF),
                onClick = { onNavigateTo("VungleTest") }
            )

            NetworkNavigationCard(
                name = "Pangle Ads",
                description = "TikTok Audience Network banner, interstitial, and rewarded ads.",
                color = Color(0xFFFF2C55),
                onClick = { onNavigateTo("PangleTest") }
            )

            NetworkNavigationCard(
                name = "IronSource (LevelPlay)",
                description = "IronSource banner, interstitial, and rewarded mediation ads.",
                color = Color(0xFF2862EC),
                onClick = { onNavigateTo("IronSourceTest") }
            )
        }

        Spacer(modifier = Modifier.height(48.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetworkNavigationCard(
    name: String,
    description: String,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Color indicator dot
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(color)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Navigate",
                tint = MaterialTheme.colorScheme.outline
            )
        }
    }
}
