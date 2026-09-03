package io.github.saifullah.nurani.ads.multi

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
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
        onAdFailedToLoad = { error -> logAdEvent(network, "$format failed to load (code: ${error?.code}): ${error?.message ?: "Unknown"}") }
    )
}

fun createContentCallback(network: String, format: String): AdContentCallback {
    return AdContentCallback(
        onAdShowed = { logAdEvent(network, "$format showed") },
        onAdDisplayed = { logAdEvent(network, "$format displayed") },
        onAdDismissed = { logAdEvent(network, "$format dismissed") },
        onAdClicked = { logAdEvent(network, "$format clicked") },
        onAdFailedToShow = { error -> logAdEvent(network, "$format failed to show (code: ${error?.code}): ${error?.message ?: "Unknown"}") }
    )
}

fun createBannerListener(network: String): BannerAdListener {
    return BannerAdListener(
        onAdLoaded = { logAdEvent(network, "Banner loaded successfully") },
        onAdFailedToLoad = { error -> logAdEvent(network, "Banner failed to load (code: ${error?.code}): ${error?.message ?: "Unknown"}") },
        onAdClicked = { logAdEvent(network, "Banner clicked") },
        onAdDisplayed = { logAdEvent(network, "Banner displayed") },
        onAdDismissed = { logAdEvent(network, "Banner dismissed") }
    )
}

@Composable
fun AdConsoleCard(modifier: Modifier = Modifier) {
    val isDark = isSystemInDarkTheme()
    val consoleBg = if (isDark) Color(0xFF14151B) else Color(0xFFF8FAFC)
    val consoleBorder = if (isDark) Color(0xFF2D3142) else Color(0xFFE2E8F0)
    val errorColor = if (isDark) Color(0xFFFF6B6B) else Color(0xFFDC2626)
    val successColor = if (isDark) Color(0xFF4ADE80) else Color(0xFF16A34A)
    val infoColor = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7)
    val emptyTextColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)

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
                    .height(160.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(consoleBg)
                    .border(1.dp, consoleBorder, RoundedCornerShape(8.dp))
                    .padding(10.dp),
                contentAlignment = Alignment.Center
            ) {
                if (globalAdLogs.isEmpty()) {
                    Text(
                        text = "Console is empty. Click buttons above to trigger ad events.",
                        color = emptyTextColor,
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
                                color = if (log.contains("Failed") || log.contains("failed") || log.contains("Error") || log.contains("error")) errorColor 
                                        else if (log.contains("success") || log.contains("Loaded") || log.contains("Showed") || log.contains("User rewarded") || log.contains("displayed")) successColor 
                                        else infoColor,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Medium,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }
    }
}
