package org.syriacplatform

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.syriacplatform.bootstrap.PlatformBootstrap
import org.syriacplatform.common.result.Result
import org.syriacplatform.common.types.QoloId
import org.syriacplatform.content.models.Qolo
import org.syriacplatform.context.PlatformContext
import org.syriacplatform.navigation.AppDestination

@Composable
fun App() {
    val platform = remember {
        PlatformBootstrap.create()
    }

    val navigationState by
    platform.navigation.state.collectAsState()

    MaterialTheme {
        when (navigationState.currentDestination) {
            AppDestination.HOME -> {
                HomeScreen(
                    onOpenQolo = {
                        platform.navigation.navigateTo(
                            AppDestination.QOLO_DETAILS
                        )
                    }
                )
            }

            AppDestination.QOLO_DETAILS -> {
                QoloDetailsScreen(
                    platform = platform,
                    onBack = {
                        platform.navigation.navigateTo(
                            AppDestination.HOME
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun HomeScreen(
    onOpenQolo: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                MaterialTheme.colorScheme.primaryContainer
            )
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "SyriacPlatform",
            style = MaterialTheme.typography.headlineMedium
        )

        Button(
            onClick = onOpenQolo,
            modifier = Modifier.padding(top = 24.dp)
        ) {
            Text(
                text = "Open Demo Qolo"
            )
        }
    }
}

@Composable
private fun QoloDetailsScreen(
    platform: PlatformContext,
    onBack: () -> Unit
) {
    val qoloName = remember(platform) {
        loadDemoQoloName(platform)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                MaterialTheme.colorScheme.primaryContainer
            )
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Qolo Details",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = qoloName,
            modifier = Modifier.padding(top = 24.dp),
            style = MaterialTheme.typography.headlineLarge,
            textAlign = TextAlign.Center
        )

        Button(
            onClick = onBack,
            modifier = Modifier.padding(top = 24.dp)
        ) {
            Text(
                text = "Back"
            )
        }
    }
}

private fun loadDemoQoloName(
    platform: PlatformContext
): String {
    return when (
        val qoloResult =
            platform.content.loadQolo(QoloId(1))
    ) {
        is Result.Success<Qolo> ->
            qoloResult.data.name

        is Result.Failure ->
            qoloResult.error.message
                ?: "Qolo loading failed"
    }
}