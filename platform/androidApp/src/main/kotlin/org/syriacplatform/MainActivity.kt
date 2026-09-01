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

class MainActivity : ComponentActivity() {

    private var platformContext: PlatformContext? =
        null

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

        setContent {
            App(
                platform = platform
            )
        }
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