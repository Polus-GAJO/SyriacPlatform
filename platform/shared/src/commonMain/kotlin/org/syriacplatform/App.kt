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
import org.syriacplatform.common.result.Result
import org.syriacplatform.common.types.QoloId
import org.syriacplatform.content.contracts.ContentService
import org.syriacplatform.content.models.Qolo
import org.syriacplatform.content.services.DefaultContentService
import org.syriacplatform.kernel.PlatformKernel

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
    val kernel = PlatformKernel()
    val contentService = DefaultContentService()

    contentService.initialize()

    kernel.registerService(
        ContentService::class,
        contentService
    )

    val resolvedService = kernel.resolveService(ContentService::class)

    return when (resolvedService) {
        is Result.Success -> {
            when (val qoloResult = resolvedService.data.loadQolo(QoloId(1))) {
                is Result.Success<Qolo> -> qoloResult.data.name
                is Result.Failure -> qoloResult.error.message ?: "Qolo loading failed"
            }
        }

        is Result.Failure -> {
            resolvedService.error.message ?: "Content Service is unavailable"
        }
    }
}