package org.syriacplatform

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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

    var selectedQoloId by remember {
        mutableStateOf<QoloId?>(null)
    }

    MaterialTheme {
        when (navigationState.currentDestination) {
            AppDestination.HOME -> {
                HomeScreen(
                    platform = platform,
                    onOpenQolo = { qoloId ->
                        selectedQoloId = qoloId

                        platform.navigation.navigateTo(
                            AppDestination.QOLO_DETAILS
                        )
                    }
                )
            }

            AppDestination.QOLO_DETAILS -> {
                val qoloId = selectedQoloId

                if (qoloId != null) {
                    QoloDetailsScreen(
                        platform = platform,
                        qoloId = qoloId,
                        onBack = {
                            selectedQoloId = null

                            platform.navigation.navigateTo(
                                AppDestination.HOME
                            )
                        }
                    )
                } else {
                    MissingQoloSelectionScreen(
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
}

@Composable
private fun HomeScreen(
    platform: PlatformContext,
    onOpenQolo: (QoloId) -> Unit
) {
    val qolosResult by produceState<Result<List<Qolo>>?>(
        initialValue = null,
        key1 = platform
    ) {
        value = platform.content.loadAllQolos()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                MaterialTheme.colorScheme.primaryContainer
            )
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "SyriacPlatform",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = "Qolos",
            modifier = Modifier.padding(
                top = 8.dp,
                bottom = 24.dp
            ),
            style = MaterialTheme.typography.titleLarge
        )

        when (
            val result = qolosResult
        ) {
            null -> {
                Text(
                    text = "Loading..."
                )
            }

            is Result.Failure -> {
                Text(
                    text = result.error.message
                        ?: "Qolos loading failed",
                    textAlign = TextAlign.Center
                )
            }

            is Result.Success<List<Qolo>> -> {
                val qolos = result.data

                if (qolos.isEmpty()) {
                    Text(
                        text = "No Qolos available"
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement =
                            Arrangement.spacedBy(12.dp)
                    ) {
                        items(qolos) { qolo ->
                            QoloListItem(
                                qolo = qolo,
                                onClick = {
                                    onOpenQolo(qolo.id)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QoloListItem(
    qolo: Qolo,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = qolo.name,
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center
            )

            qolo.poeticMeter?.let { poeticMeter ->
                Text(
                    text = "Poetic meter: $poeticMeter",
                    modifier = Modifier.padding(top = 4.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun QoloDetailsScreen(
    platform: PlatformContext,
    qoloId: QoloId,
    onBack: () -> Unit
) {
    val qoloResult by produceState<Result<Qolo>?>(
        initialValue = null,
        key1 = platform,
        key2 = qoloId
    ) {
        value = platform.content.loadQolo(qoloId)
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

        when (
            val result = qoloResult
        ) {
            null -> {
                Text(
                    text = "Loading...",
                    modifier = Modifier.padding(top = 24.dp)
                )
            }

            is Result.Failure -> {
                Text(
                    text = result.error.message
                        ?: "Qolo loading failed",
                    modifier = Modifier.padding(top = 24.dp),
                    textAlign = TextAlign.Center
                )
            }

            is Result.Success<Qolo> -> {
                val qolo = result.data

                Text(
                    text = qolo.name,
                    modifier = Modifier.padding(top = 24.dp),
                    style =
                        MaterialTheme.typography.headlineLarge,
                    textAlign = TextAlign.Center
                )

                qolo.poeticMeter?.let { poeticMeter ->
                    Text(
                        text = "Poetic meter: $poeticMeter",
                        modifier = Modifier.padding(top = 12.dp),
                        style =
                            MaterialTheme.typography.titleMedium
                    )
                }
            }
        }

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

@Composable
private fun MissingQoloSelectionScreen(
    onBack: () -> Unit
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
            text = "No Qolo was selected.",
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