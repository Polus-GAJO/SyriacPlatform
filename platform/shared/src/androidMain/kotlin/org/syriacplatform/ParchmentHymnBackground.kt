package org.syriacplatform

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.height
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext

/** Shared parchment layer for the hymn details screen in both Android apps. */
@Composable
fun ParchmentHymnBackground(enabled: Boolean = true, content: @Composable () -> Unit) {
    val context = LocalContext.current
    val parchment = remember(context, enabled) {
        if (!enabled) return@remember null
        try {
            context.assets.open("shhimo-parchment.jpg").use { stream ->
                BitmapFactory.decodeStream(stream)?.asImageBitmap()
            }
        } catch (_: java.io.IOException) {
            null
        }
    }
    Box(Modifier.fillMaxSize()) {
        if (parchment != null) {
            Image(
                bitmap = parchment,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
        if (enabled && parchment != null) {
            Box(
                Modifier.align(Alignment.BottomCenter)
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            0.0f to Color.Transparent,
                            0.78f to Color.Transparent,
                            1.0f to Color.Black.copy(alpha = 0.35f)
                        )
                    )
            )
        }
        CompositionLocalProvider(
            LocalParchmentHymnBackground provides (enabled && parchment != null)
        ) {
            content()
        }
    }
}
