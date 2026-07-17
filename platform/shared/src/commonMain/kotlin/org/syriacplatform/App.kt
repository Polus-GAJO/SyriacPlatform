package org.syriacplatform

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.syriacplatform.bootstrap.PlatformBootstrap
import org.syriacplatform.common.result.Result
import org.syriacplatform.common.types.QoloId
import org.syriacplatform.content.models.Qolo

@Composable
fun App() {
    val displayText = remember {
        loadDemoQoloName()
    }

    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "SyriacPlatform",
                style = MaterialTheme.typography.headlineMedium
            )

            Text(
                text = displayText,
                modifier = Modifier.padding(top = 24.dp),
                style = MaterialTheme.typography.headlineLarge,
                textAlign = TextAlign.Center
            )
        }
    }
}

private fun loadDemoQoloName(): String {
    val platform = PlatformBootstrap.create()

    return when (
        val qoloResult =
            platform.content.loadQolo(QoloId(1))
    ) {
        is Result.Success<Qolo> ->
            qoloResult.data.name

        is Result.Failure ->
            qoloResult.error.message ?: "Qolo loading failed"
    }
}