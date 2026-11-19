package org.syriacplatform.presentation.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp

/**
 * Language-aware liturgical styles. The caller supplies the current size,
 * preserving approved screen typography during incremental migration.
 *
 * A later font replacement only changes LiturgicalFontFamilies.
 */
enum class LiturgicalTitleLevel { Occasion, Prayer, Hymn }

object LiturgicalTextStyles {
    /**
     * Keep all three title levels at the currently approved 26sp for now.
     * Levels are distinct so a later hierarchy can be introduced centrally.
     */
    @Composable
    fun title(level: LiturgicalTitleLevel): TextStyle {
        val sizeSp = when (level) {
            LiturgicalTitleLevel.Occasion -> LiturgicalTypography.SyriacBaseSp
            LiturgicalTitleLevel.Prayer -> LiturgicalTypography.SyriacBaseSp
            LiturgicalTitleLevel.Hymn -> LiturgicalTypography.SyriacBaseSp
        }
        return TextStyle(
            fontFamily = LiturgicalFontFamilies.syriac(),
            fontSize = sizeSp.sp,
            textAlign = TextAlign.Center,
            textDirection = TextDirection.Rtl,
            color = LiturgicalColors.Red
        )
    }

    /** Section headings retain the existing Syriac body size until visual review. */
    @Composable
    fun section(): TextStyle = TextStyle(
        fontFamily = LiturgicalFontFamilies.syriac(),
        fontSize = LiturgicalTypography.SyriacBaseSp.sp,
        textAlign = TextAlign.Center,
        textDirection = TextDirection.Rtl,
        color = LiturgicalColors.Red
    )

    /** Preserve each description's current Material typography and language until reviewed. */
    fun secondary(base: TextStyle): TextStyle = base.copy(color = LiturgicalColors.Black)

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
