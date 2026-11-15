package org.syriacplatform

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import org.syriacplatform.audio.android.AndroidAudioPlayerBackend
import org.syriacplatform.bootstrap.DefaultPlatformDependencies
import org.syriacplatform.bootstrap.PlatformBootstrap
import org.syriacplatform.context.PlatformContext
import org.syriacplatform.navigation.AppSessionState
import org.syriacplatform.navigation.AppDestination
import org.syriacplatform.common.types.OccasionId
import org.syriacplatform.common.types.PrayerSequenceId
import org.syriacplatform.common.types.LiturgicalItemId
import org.syriacplatform.common.types.QoloId

class MainActivity : ComponentActivity() {

    private var platformContext: PlatformContext? =
        null

    private var sessionSnapshot = AppSessionState()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val backend =
            AndroidAudioPlayerBackend(this)

        val dependencies =
            DefaultPlatformDependencies
                .create()
                .copy(
                    audioPlayerBackend =
                        backend
                )

        val platform =
            PlatformBootstrap.create(
                dependencies = dependencies
            )

        platformContext =
            platform

        sessionSnapshot = restoreSession(savedInstanceState).normalized()
        platform.navigation.navigateTo(sessionSnapshot.destination)

        setContent {
            App(
                platform = platform,
                initialSession = sessionSnapshot,
                onSessionChanged = { sessionSnapshot = it }
            )
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        val snapshot = sessionSnapshot.normalized()
        outState.putString("session.destination", snapshot.destination.name)
        snapshot.occasionId?.let { outState.putLong("session.occasion", it.value) }
        snapshot.prayerSequenceId?.let { outState.putLong("session.prayer", it.value) }
        snapshot.liturgicalItemId?.let { outState.putLong("session.item", it.value) }
        snapshot.qoloId?.let { outState.putLong("session.qolo", it.value) }
        super.onSaveInstanceState(outState)
    }

    private fun restoreSession(saved: Bundle?): AppSessionState {
        if (saved == null) return AppSessionState()
        val destination = AppDestination.entries.firstOrNull {
            it.name == saved.getString("session.destination")
        } ?: AppDestination.HOME
        return AppSessionState(
            destination = destination,
            occasionId = if (saved.containsKey("session.occasion"))
                OccasionId(saved.getLong("session.occasion")) else null,
            prayerSequenceId = if (saved.containsKey("session.prayer"))
                PrayerSequenceId(saved.getLong("session.prayer")) else null,
            liturgicalItemId = if (saved.containsKey("session.item"))
                LiturgicalItemId(saved.getLong("session.item")) else null,
            qoloId = if (saved.containsKey("session.qolo"))
                QoloId(saved.getLong("session.qolo")) else null
        )
    }

    override fun onDestroy() {
        platformContext?.shutdown()
        platformContext = null
        super.onDestroy()
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    val platform =
        remember {
            PlatformBootstrap.create()
        }

    App(
        platform = platform
    )
}