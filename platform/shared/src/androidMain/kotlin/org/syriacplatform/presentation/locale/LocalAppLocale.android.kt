package org.syriacplatform.presentation.locale

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidedValue
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import java.util.Locale

actual object LocalAppLocale {
    private val originalLocale: Locale = Locale.getDefault()

    actual val current: String
        @Composable get() = LocalConfiguration.current.locales[0]?.language
            ?: originalLocale.language

    @Composable
    actual infix fun provides(value: String?): ProvidedValue<*> {
        val context = LocalContext.current
        val configuration = android.content.res.Configuration(LocalConfiguration.current)
        val locale = value?.let { Locale.forLanguageTag(it) } ?: originalLocale
        Locale.setDefault(locale)
        configuration.setLocale(locale)
        @Suppress("DEPRECATION")
        context.resources.updateConfiguration(configuration, context.resources.displayMetrics)
        return LocalConfiguration.provides(configuration)
    }
}
