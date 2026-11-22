package org.syriacplatform

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.weight
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModelProvider
import org.syriacplatform.audio.models.PlaybackStatus
import org.syriacplatform.packageformat.loading.PackageSource
import org.syriacplatform.presentation.audio.CompactAudioPlayer
import org.syriacplatform.presentation.shhimo.ShhimoDailyScreen

/** Shhimo uses the same hymn detail UI as the Occasion app. */
class ShhimoActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val session = ViewModelProvider(this)[ShhimoSessionViewModel::class.java]
        val audio = session.audio(applicationContext)
        val source = object : PackageSource {
            override suspend fun readBytesOrNull(path: String): ByteArray? =
                try {
                    assets.open("shhimo/$path").use { it.readBytes() }
                } catch (_: java.io.FileNotFoundException) {
                    null
                }
        }
        setContent {
            val playback by audio.state.collectAsState()
            Column {
                val hymnId = session.selectedHymnId
                if (hymnId != null) {
                    ShhimoHymnDetails(
                        source = source,
                        audioService = audio,
                        itemId = hymnId,
                        selectedMelodyId = session.selectedMelodyId,
                        notationOpen = session.notationOpen,
                        notationPage = session.notationPage,
                        recordingIds = session.recordingIds,
                        onMelodyChanged = { session.selectedMelodyId = it },
                        onNotationChanged = { open, page ->
                            session.notationOpen = open
                            session.notationPage = page
                        },
                        onRecordingSelected = { melody, recording ->
                            session.recordingIds[melody] = recording
                        },
                        onBack = {
                            session.notationOpen = false
                            session.notationPage = 0
                            session.selectedHymnId = null
                        }
                    )
                } else {
                    ShhimoDailyScreen(
                        source = source,
                        navigationState = session.navigation,
                        onOpenLiturgicalItem = { id ->
                            audio.stop()
                            session.selectedMelodyId = null
                            session.notationOpen = false
                            session.notationPage = 0
                            session.selectedHymnId = id
                        },
                        modifier = androidx.compose.ui.Modifier.weight(1f)
                    )
                    if (playback.status != PlaybackStatus.Idle) {
                        CompactAudioPlayer(
                            title = "تسجيل الأداء",
                            status = playback.status,
                            positionMs = playback.positionMs,
                            durationMs = playback.durationMs,
                            onTogglePlayback = {
                                when (playback.status) {
                                    PlaybackStatus.Playing -> audio.pause()
                                    PlaybackStatus.Ready, PlaybackStatus.Paused,
                                    PlaybackStatus.Ended -> audio.play()
                                    else -> Unit
                                }
                            },
                            onSeek = { audio.seekTo(it) },
                            onClose = { audio.stop() }
                        )
                    }
                }
            }
        }
    }
}
