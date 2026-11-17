package org.syriacplatform.presentation.audio

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import org.syriacplatform.audio.models.PlaybackStatus

/**
 * Compact visual controller only. The caller owns AudioService and playback state.
 * RTL is applied to the seek bar itself, independently of the app's text direction.
 */
@Composable
fun CompactAudioPlayer(
    title: String,
    status: PlaybackStatus,
    positionMs: Long,
    durationMs: Long?,
    intervalStartMs: Long? = null,
    intervalEndMs: Long? = null,
    onTogglePlayback: () -> Unit,
    onSeek: (Long) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val start = intervalStartMs ?: 0L
    val end = intervalEndMs ?: durationMs ?: 0L
    val duration = (end - start).coerceAtLeast(0L)
    val position = (positionMs - start).coerceIn(0L, duration)
    val canSeek = duration > 0L &&
        status != PlaybackStatus.Loading &&
        status != PlaybackStatus.Error &&
        status != PlaybackStatus.Idle
    val playing = status == PlaybackStatus.Playing

    Surface(
        modifier = modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 3.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.90f),
        shadowElevation = 5.dp
    ) {
        Column(modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)) {
            Text(
                text = title,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Right,
                style = MaterialTheme.typography.labelSmall
            )
        Row(
            modifier = Modifier,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(formatAudioTime(position), style = MaterialTheme.typography.labelSmall)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 6.dp)
                            .height(24.dp)
                            .pointerInput(start, duration, canSeek) {
                                detectTapGestures { offset ->
                                    if (canSeek) {
                                        val fraction = (1f - offset.x / size.width)
                                            .coerceIn(0f, 1f)
                                        onSeek(start + (fraction * duration).toLong())
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(3.dp)
                                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f), RoundedCornerShape(3.dp))
                        )
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Box(
                                modifier = Modifier.fillMaxWidth(
                                    if (duration > 0L) position.toFloat() / duration else 0f
                                ).height(3.dp)
                                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(3.dp))
                            )
                        }
                    }
                    Text(formatAudioTime(duration), style = MaterialTheme.typography.labelSmall)
                }
            }
            IconButton(
                enabled = status != PlaybackStatus.Loading && status != PlaybackStatus.Error,
                onClick = onTogglePlayback
            ) {
                Text(if (playing) "Ⅱ" else "▶", style = MaterialTheme.typography.titleLarge)
            }
            IconButton(onClick = onClose) {
                Text("×", style = MaterialTheme.typography.titleLarge)
            }
        }
        }
    }
}

internal fun formatAudioTime(milliseconds: Long): String {
    val seconds = milliseconds.coerceAtLeast(0L) / 1000L
    val minutes = seconds / 60L
    val remainder = seconds % 60L
    return "$minutes:${remainder.toString().padStart(2, '0')}"
}
