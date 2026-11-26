package org.syriacplatform.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import org.syriacplatform.presentation.locale.LocalAppLocale

/** Keep existing Material sizing and use the bundled Arabic family for Arabic UI labels. */
@Composable
fun PlatformTypographyTheme(content: @Composable () -> Unit) {
    val base = MaterialTheme.typography
    val locale = LocalAppLocale.current.substringBefore('-').substringBefore('_').lowercase()
    val typography = if (locale == "ar") {
        val family = LiturgicalFontFamilies.arabic()
        base.copy(
            displayLarge = base.displayLarge.copy(fontFamily = family),
            displayMedium = base.displayMedium.copy(fontFamily = family),
            displaySmall = base.displaySmall.copy(fontFamily = family),
            headlineLarge = base.headlineLarge.copy(fontFamily = family),
            headlineMedium = base.headlineMedium.copy(fontFamily = family),
            headlineSmall = base.headlineSmall.copy(fontFamily = family),
            titleLarge = base.titleLarge.copy(fontFamily = family),
            titleMedium = base.titleMedium.copy(fontFamily = family),
            titleSmall = base.titleSmall.copy(fontFamily = family),
            bodyLarge = base.bodyLarge.copy(fontFamily = family),
            bodyMedium = base.bodyMedium.copy(fontFamily = family),
            bodySmall = base.bodySmall.copy(fontFamily = family),
            labelLarge = base.labelLarge.copy(fontFamily = family),
            labelMedium = base.labelMedium.copy(fontFamily = family),
            labelSmall = base.labelSmall.copy(fontFamily = family)
        )
    } else base
    MaterialTheme(typography = typography, content = content)
}

/** Explicit font for localized controls, including Arabic labels in mixed-language UI. */
@Composable
fun localizedControlFont(text: String): androidx.compose.ui.text.font.FontFamily? =
    if (text.any { it in '\u0600'..'\u06FF' || it in '\u0750'..'\u077F' || it in '\u08A0'..'\u08FF' })
        LiturgicalFontFamilies.arabic()
    else null

@Composable
fun LocalizedControlText(text: String) {
    androidx.compose.material3.Text(text = text, fontFamily = localizedControlFont(text))
}
