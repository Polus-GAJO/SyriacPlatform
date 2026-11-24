package org.syriacplatform

import android.os.Bundle
import org.syriacplatform.presentation.shhimo.shhimoRecordingLabel
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import org.syriacplatform.presentation.shhimo.ShhimoQintoSelector
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import kotlinx.coroutines.launch
import org.syriacplatform.presentation.locale.LocalAppLocale
import org.syriacplatform.presentation.shhimo.ShhimoReadingSettings
import org.syriacplatform.user.storage.AndroidUserPreferencesStore
import androidx.lifecycle.ViewModelProvider
import org.syriacplatform.audio.models.PlaybackStatus
import org.syriacplatform.packageformat.loading.PackageSource
import org.syriacplatform.presentation.audio.CompactAudioPlayer
import org.syriacplatform.presentation.shhimo.ShhimoDailyScreen

/** Shhimo uses the same hymn detail UI as the Occasion app. */
class ShhimoActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        window.navigationBarColor = android.graphics.Color.TRANSPARENT
        if (android.os.Build.VERSION.SDK_INT >= 29) window.isNavigationBarContrastEnforced = false
        val session = ViewModelProvider(this)[ShhimoSessionViewModel::class.java]
        val qintoPreferences = getSharedPreferences("syriacplatform.shhimo.user.preferences.v1", MODE_PRIVATE)
        session.selectWeeklyQinto(qintoPreferences.getLong("weekly_qinto_id", 1L).coerceIn(1L, 8L))
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
            val preferenceStore = remember { AndroidUserPreferencesStore(applicationContext) }
            val preferences by preferenceStore.preferences.collectAsState()
            val scope = rememberCoroutineScope()
            val systemDensity = LocalDensity.current
            val scaledDensity = remember(systemDensity, preferences.fontScale) {
                Density(systemDensity.density, systemDensity.fontScale * preferences.fontScale)
            }
            val deviceDirection = LocalLayoutDirection.current
            val direction = when (preferences.interfaceLanguage) {
                "ar" -> LayoutDirection.Rtl
                "en", "fr", "de" -> LayoutDirection.Ltr
                else -> deviceDirection
            }
            val playback by audio.state.collectAsState()
            CompositionLocalProvider(
                LocalDensity provides scaledDensity,
                LocalAppLocale provides preferences.interfaceLanguage,
                LocalLayoutDirection provides direction
            ) {
            Column {
                val hymnId = session.selectedHymnId
                if (hymnId != null) {
                    ParchmentHymnBackground {
                    ShhimoHymnDetails(
                        source = source,
                        audioService = audio,
                        itemId = hymnId,
                        weeklyQintoId = session.weeklyQintoId,
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
                        onNextItem = session.prayerItemIds.getOrNull(session.prayerItemIds.indexOf(hymnId) + 1)?.let { nextId ->
                            {
                                audio.stop()
                                session.selectedMelodyId = null
                                session.notationOpen = false
                                session.notationPage = 0
                                session.selectedHymnId = nextId
                            }
                        },
                        onBack = {
                            session.notationOpen = false
                            session.notationPage = 0
                            session.selectedHymnId = null
                        }
                    )
                    }
                } else {
                    ShhimoDailyScreen(
                        source = source,
                        navigationState = session.navigation,
                        onOpenLiturgicalItem = { id, prayerItems ->
                            session.prayerItemIds = prayerItems
                            audio.stop()
                            session.selectedMelodyId = null
                            session.notationOpen = false
                            session.notationPage = 0
                            session.selectedHymnId = id
                        },
                        qintoSelector = {
                            ShhimoQintoSelector(session.weeklyQintoId) { id ->
                                if (session.weeklyQintoId != id) {
                                    qintoPreferences.edit().putLong("weekly_qinto_id", id).apply()
                                    audio.stop()
                                    session.selectWeeklyQinto(id)
                                }
                            }
                        },
                        settings = {
                            ShhimoReadingSettings(preferences) { updated ->
                                scope.launch { preferenceStore.save(updated) }
                            }
                        },
                        modifier = androidx.compose.ui.Modifier.weight(1f)
                    )
                    if (playback.status != PlaybackStatus.Idle) {
                        CompactAudioPlayer(
                            title = shhimoRecordingLabel(),
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
}
