package org.syriacplatform.presentation.audio

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextOverflow
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
    onTogglePlayback: () -> Unit,
    onSeek: (Long) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val duration = (durationMs ?: 0L).coerceAtLeast(0L)
    val position = positionMs.coerceIn(0L, duration)
    val canSeek = duration > 0L &&
        status != PlaybackStatus.Loading &&
        status != PlaybackStatus.Error &&
        status != PlaybackStatus.Idle
    val playing = status == PlaybackStatus.Playing

    Surface(
        modifier = modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.90f),
        shadowElevation = 5.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.labelMedium
                )
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(formatAudioTime(position), style = MaterialTheme.typography.labelSmall)
                        Slider(
                            value = if (duration > 0L) position.toFloat() / duration else 0f,
                            onValueChange = { fraction ->
                                if (canSeek) onSeek((fraction * duration).toLong().coerceIn(0L, duration))
                            },
                            enabled = canSeek,
                            modifier = Modifier.weight(1f).padding(horizontal = 4.dp)
                        )
                        Text(formatAudioTime(duration), style = MaterialTheme.typography.labelSmall)
                    }
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

internal fun formatAudioTime(milliseconds: Long): String {
    val seconds = milliseconds.coerceAtLeast(0L) / 1000L
    val minutes = seconds / 60L
    val remainder = seconds % 60L
    return "$minutes:${remainder.toString().padStart(2, '0')}"
}
