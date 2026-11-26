package org.syriacplatform.presentation.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import org.jetbrains.compose.resources.Font
import org.syriacplatform.resources.Res
import org.syriacplatform.resources.noto_naskh_arabic_regular
import org.syriacplatform.resources.noto_naskh_arabic_medium
import org.syriacplatform.resources.noto_naskh_arabic_bold
import org.syriacplatform.resources.noto_sans
import org.syriacplatform.resources.serto_jerusalem

/**
 * Shared bundled font families for liturgical content.
 *
 * Font resources travel with the application, rather than depending on
 * fonts installed on Android, macOS or other host systems.
 */
object LiturgicalFontFamilies {
    @Composable
    fun syriac(): FontFamily = FontFamily(Font(Res.font.serto_jerusalem))

    @Composable
    fun arabic(): FontFamily = FontFamily(
        Font(Res.font.noto_naskh_arabic_regular, weight = FontWeight.Normal),
        Font(Res.font.noto_naskh_arabic_medium, weight = FontWeight.Medium),
        Font(Res.font.noto_naskh_arabic_bold, weight = FontWeight.Bold)
    )

    @Composable
    fun latin(): FontFamily = FontFamily(Font(Res.font.noto_sans))

    @Composable
    fun forLanguage(language: LiturgicalLanguage): FontFamily = when (language) {
        LiturgicalLanguage.Syriac -> syriac()
        LiturgicalLanguage.Arabic -> arabic()
        LiturgicalLanguage.English, LiturgicalLanguage.French -> latin()
    }
}
