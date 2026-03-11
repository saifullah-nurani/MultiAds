package io.github.saifullah.nurani.ads.multi

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    MaterialTheme {
        var currentScreen by remember { mutableStateOf("Home") }

        if (currentScreen == "Home") {
            HomeScreen(
                onNavigateToAdmob = { currentScreen = "AdmobTest" }
            )
        } else if (currentScreen == "AdmobTest") {
            AdmobTestScreen(
                onBack = { currentScreen = "Home" }
            )
        }
    }
}

@Composable
fun HomeScreen(onNavigateToAdmob: () -> Unit) {
    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.primaryContainer)
            .safeContentPadding()
            .fillMaxSize(),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        Button(onClick = onNavigateToAdmob) {
            Text("Go to Admob Test Screen")
        }
    }
}