package org.syriacplatform

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
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
        CompositionLocalProvider(
            LocalParchmentHymnBackground provides (enabled && parchment != null)
        ) {
            content()
        }
    }
}
