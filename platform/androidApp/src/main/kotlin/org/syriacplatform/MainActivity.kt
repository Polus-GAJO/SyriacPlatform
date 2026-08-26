package org.syriacplatform

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import org.syriacplatform.audio.android.AndroidAudioPlayerBackend
import org.syriacplatform.audio.resources.ComposeResourceMediaResourceResolver
import org.syriacplatform.audio.services.DefaultAudioService

class MainActivity : ComponentActivity() {

    private var audioBackend: AndroidAudioPlayerBackend? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val backend = AndroidAudioPlayerBackend(this)
        audioBackend = backend

        val audioService =
            DefaultAudioService(
                resourceResolver =
                    ComposeResourceMediaResourceResolver(),
                playerBackend =
                    backend
            ).also {
                it.initialize()
            }

        setContent {
            App(audioService = audioService)
        }
    }

    override fun onDestroy() {
        audioBackend?.release()
        audioBackend = null
        super.onDestroy()
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
