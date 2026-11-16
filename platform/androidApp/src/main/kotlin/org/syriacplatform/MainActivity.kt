package org.syriacplatform

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
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
import org.syriacplatform.common.types.MelodyId

class RetainedPlatformModel : ViewModel() {
    private var retained: PlatformContext? = null

    fun getOrCreate(activity: ComponentActivity): PlatformContext {
        retained?.let { return it }
        val backend = AndroidAudioPlayerBackend(activity.applicationContext)
        val dependencies = DefaultPlatformDependencies.create().copy(
            audioPlayerBackend = backend
        )
        return PlatformBootstrap.create(dependencies).also { retained = it }
    }

    override fun onCleared() {
        retained?.shutdown()
        retained = null
        super.onCleared()
    }
}

class MainActivity : ComponentActivity() {

    private var sessionSnapshot = AppSessionState()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val platform = ViewModelProvider(this)[RetainedPlatformModel::class.java]
            .getOrCreate(this)

        sessionSnapshot = restoreSession(savedInstanceState).normalized()
        platform.navigation.navigateTo(sessionSnapshot.destination)

        setContent {
            App(
                platform = platform,
                initialSession = sessionSnapshot,
                onSessionChanged = { updated ->
                    sessionSnapshot = updated
                }
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
        snapshot.melodyId?.let { outState.putLong("session.melody", it.value) }
        outState.putBoolean("session.notationOpen", snapshot.notationViewerOpen)
        outState.putInt("session.notationPage", snapshot.notationPage)
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
                QoloId(saved.getLong("session.qolo")) else null,
            melodyId = if (saved.containsKey("session.melody"))
                MelodyId(saved.getLong("session.melody")) else null,
            notationViewerOpen = saved.getBoolean("session.notationOpen", false),
            notationPage = saved.getInt("session.notationPage", 0)
        )
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