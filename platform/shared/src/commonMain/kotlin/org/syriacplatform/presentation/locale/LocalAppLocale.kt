package org.syriacplatform.presentation.locale

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidedValue

@Composable
expect fun AppLocaleProvider(language: String?, content: @Composable () -> Unit)

/**
 * Locale override for Compose Multiplatform string resources.
 * A null override follows the device language.
 */
expect object LocalAppLocale {
    val current: String @Composable get
    @Composable infix fun provides(value: String?): ProvidedValue<*>
}
