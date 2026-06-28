package io.github.saifullah.nurani.ads.multi

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.saifullah.nurani.ads.core.AdLoadCallback
import io.github.saifullah.nurani.ads.core.AdContentCallback
import io.github.saifullah.nurani.ads.core.BannerAdListener
import io.github.saifullah.nurani.ads.core.AdError

val globalAdLogs = mutableStateListOf<String>()

fun logAdEvent(tag: String, message: String) {
    println("[$tag] $message")
    globalAdLogs.add(0, "[$tag] $message")
    if (globalAdLogs.size > 50) {
        globalAdLogs.removeLast()
    }
}

fun createLoadCallback(network: String, format: String): AdLoadCallback {
    return AdLoadCallback(
        onAdLoaded = { logAdEvent(network, "$format loaded successfully") },
        onAdFailedToLoad = { error -> logAdEvent(network, "$format failed to load: ${error?.message ?: "Unknown"}") }
    )
}

fun createContentCallback(network: String, format: String): AdContentCallback {
    return AdContentCallback(
        onAdShowed = { logAdEvent(network, "$format showed") },
        onAdDisplayed = { logAdEvent(network, "$format displayed") },
        onAdDismissed = { logAdEvent(network, "$format dismissed") },
        onAdClicked = { logAdEvent(network, "$format clicked") },
        onAdFailedToShow = { error -> logAdEvent(network, "$format failed to show: ${error?.message ?: "Unknown"}") }
    )
}

fun createBannerListener(network: String): BannerAdListener {
    return BannerAdListener(
        onAdLoaded = { logAdEvent(network, "Banner loaded successfully") },
        onAdFailedToLoad = { error -> logAdEvent(network, "Banner failed to load: ${error?.message ?: "Unknown"}") },
        onAdClicked = { logAdEvent(network, "Banner clicked") },
        onAdDisplayed = { logAdEvent(network, "Banner displayed") },
        onAdDismissed = { logAdEvent(network, "Banner dismissed") }
    )
}

@Composable
fun AdConsoleCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
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
                    text = "Live Console Logs",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = { globalAdLogs.clear() }) {
                    Text("Clear")
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .background(Color.Black.copy(alpha = 0.05f))
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                if (globalAdLogs.isEmpty()) {
                    Text(
                        text = "Console is empty. Click buttons above to trigger ad events.",
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
                        items(globalAdLogs) { log ->
                            Text(
                                text = log,
                                color = if (log.contains("Failed") || log.contains("failed")) Color.Red 
                                        else if (log.contains("success") || log.contains("Loaded") || log.contains("Showed") || log.contains("User rewarded")) Color.Green 
                                        else Color.Cyan,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }
    }
}
