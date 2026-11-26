package org.syriacplatform.presentation.locale

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
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

/** Scope the resource context to the selected language without mutating Activity resources. */
@Composable
actual fun AppLocaleProvider(language: String?, content: @Composable () -> Unit) {
    val baseContext = LocalContext.current
    val baseConfiguration = LocalConfiguration.current
    val configuration = remember(baseConfiguration, language) {
        android.content.res.Configuration(baseConfiguration).apply {
            if (!language.isNullOrBlank()) setLocale(Locale.forLanguageTag(language))
        }
    }
    val localizedContext = remember(baseContext, configuration) {
        baseContext.createConfigurationContext(configuration)
    }
    CompositionLocalProvider(
        LocalContext provides localizedContext,
        LocalConfiguration provides configuration,
        content = content
    )
}
