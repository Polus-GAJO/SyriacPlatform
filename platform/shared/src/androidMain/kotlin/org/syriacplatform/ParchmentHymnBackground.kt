package org.syriacplatform

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
            // A visible, 30%-transparent floating layer over the system navigation area.
            Box(
                Modifier.align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding())
                    .background(Color.Black.copy(alpha = 0.70f))
            )
        }
        CompositionLocalProvider(
            LocalParchmentHymnBackground provides (enabled && parchment != null)
        ) {
            content()
        }
    }
}
