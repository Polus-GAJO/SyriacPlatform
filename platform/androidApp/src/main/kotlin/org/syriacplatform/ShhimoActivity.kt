package org.syriacplatform

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.syriacplatform.audio.android.AndroidAudioPlayerBackend
import org.syriacplatform.audio.contracts.MediaResourceResolver
import org.syriacplatform.audio.models.MediaResource
import org.syriacplatform.audio.models.PlaybackStatus
import org.syriacplatform.audio.services.DefaultAudioService
import org.syriacplatform.common.result.Result
import org.syriacplatform.content.models.MediaAsset
import org.syriacplatform.packageformat.loading.PackageSource
import org.syriacplatform.presentation.audio.CompactAudioPlayer
import org.syriacplatform.presentation.shhimo.ShhimoDailyScreen

/** Independent Shhimo preview; Occasion MainActivity is not modified. */
class ShhimoActivity : ComponentActivity() {
    private lateinit var audio: DefaultAudioService
    private var pendingAutoplay by mutableStateOf(false)
    private var playingTitle by mutableStateOf("تسجيل الأداء")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val resolver = object : MediaResourceResolver {
            override fun resolve(mediaAsset: MediaAsset): Result<MediaResource> {
                // Paths in the package are relative to its root (media/...).
                return Result.Success(
                    MediaResource(mediaAsset.id, "asset:///shhimo/" + mediaAsset.path)
                )
            }
        }
        audio = DefaultAudioService(resolver, AndroidAudioPlayerBackend(applicationContext))
        audio.initialize()
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
            LaunchedEffect(playback.status, pendingAutoplay) {
                if (pendingAutoplay && playback.status == PlaybackStatus.Ready) {
                    pendingAutoplay = false
                    audio.play()
                }
                if (playback.status == PlaybackStatus.Error) pendingAutoplay = false
            }
            Column {
                ShhimoDailyScreen(
                    source = source,
                    onOpenLiturgicalItem = {},
                    onPlayRecording = { asset ->
                        pendingAutoplay = false
                        audio.stop()
                        playingTitle = asset.performer ?: "تسجيل الأداء"
                        pendingAutoplay = true
                        audio.load(asset)
                    },
                    modifier = androidx.compose.ui.Modifier.weight(1f)
                )
                if (playback.status != PlaybackStatus.Idle) {
                    CompactAudioPlayer(
                        title = playingTitle,
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
                        onClose = {
                            pendingAutoplay = false
                            audio.stop()
                        }
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        if (::audio.isInitialized) audio.shutdown()
        super.onDestroy()
    }
}
