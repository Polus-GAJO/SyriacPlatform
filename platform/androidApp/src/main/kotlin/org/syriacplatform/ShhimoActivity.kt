package org.syriacplatform

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import org.syriacplatform.packageformat.loading.PackageSource
import org.syriacplatform.presentation.shhimo.ShhimoDailyScreen

/**
 * Separate opt-in preview entry point. MainActivity / Occasion is unchanged.
 * Copy the generated book package into androidApp/src/debug/assets/shhimo/.
 */
class ShhimoActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val source = object : PackageSource {
            override suspend fun readBytesOrNull(path: String): ByteArray? =
                try {
                    assets.open("shhimo/$path").use { it.readBytes() }
                } catch (_: java.io.FileNotFoundException) {
                    null
                }
        }
        setContent {
            ShhimoDailyScreen(
                source = source,
                onOpenLiturgicalItem = { /* Detail view follows in next iteration. */ }
            )
        }
    }
}
