package org.syriacplatform.presentation.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDirection

/**
 * Language-aware liturgical styles. The caller supplies the current size,
 * preserving approved screen typography during incremental migration.
 *
 * A later font replacement only changes LiturgicalFontFamilies.
 */
object LiturgicalTextStyles {
    @Composable
    fun withLanguageFont(base: TextStyle, language: LiturgicalLanguage): TextStyle =
        base.copy(fontFamily = LiturgicalFontFamilies.forLanguage(language))

    @Composable
    fun note(base: TextStyle, language: LiturgicalLanguage): TextStyle =
        withLanguageFont(base, language).copy(
            textDirection = if (language == LiturgicalLanguage.Arabic ||
                language == LiturgicalLanguage.Syriac) TextDirection.Rtl else TextDirection.Ltr
        )
}
