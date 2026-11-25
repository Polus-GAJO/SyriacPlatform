package org.syriacplatform.presentation.shhimo

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import org.jetbrains.compose.resources.stringResource
import org.syriacplatform.resources.*
import org.syriacplatform.audio.queue.PlaybackQueueState
import org.syriacplatform.audio.queue.PlaybackQueueStatus

@Composable
fun ShhimoPrayerPlaybackControls(
    state: PlaybackQueueState,
    preparing: Boolean,
    message: String?,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    val error = when {
        message == "empty" -> stringResource(Res.string.queue_empty)
        message == "prepare" -> stringResource(Res.string.queue_prepare_failed)
        message == "start" || state.status == PlaybackQueueStatus.Error -> stringResource(Res.string.queue_start_failed)
        else -> null
    }
    if (preparing) Text(stringResource(Res.string.loading))
    if (error != null) Text(error, color = MaterialTheme.colorScheme.error)
    if (state.status in listOf(PlaybackQueueStatus.Loading, PlaybackQueueStatus.Playing, PlaybackQueueStatus.Paused)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onPrevious) { Text(stringResource(Res.string.previous)) }
            Text("${(state.currentIndex ?: 0) + 1} / ${state.entries.size}")
            TextButton(onClick = onNext) { Text(stringResource(Res.string.next)) }
        }
    }
}
