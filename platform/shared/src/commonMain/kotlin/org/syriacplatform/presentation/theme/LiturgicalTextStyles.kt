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
     * Use the approved 32/29/26sp hierarchy for occasion, prayer and hymn titles.
     * Keep these levels centralized for future visual adjustments.
     */
    @Composable
    fun title(level: LiturgicalTitleLevel): TextStyle {
        val sizeSp = when (level) {
            LiturgicalTitleLevel.Occasion -> 32f
            LiturgicalTitleLevel.Prayer -> 29f
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

    /** Section headings in other languages retain their existing Material text size. */
    @Composable
    fun section(base: TextStyle, language: LiturgicalLanguage): TextStyle =
        withLanguageFont(base, language).copy(
            textDirection = if (language == LiturgicalLanguage.Arabic ||
                language == LiturgicalLanguage.Syriac) TextDirection.Rtl else TextDirection.Ltr,
            color = LiturgicalColors.Red
        )

    /** Preserve each description's current Material typography and language until reviewed. */
    fun secondary(base: TextStyle): TextStyle = base.copy(color = LiturgicalColors.Black)

    /** Secondary labels in a known language, without changing their size. */
    @Composable
    fun secondary(base: TextStyle, language: LiturgicalLanguage): TextStyle =
        withLanguageFont(base, language).copy(
            textDirection = if (language == LiturgicalLanguage.Arabic ||
                language == LiturgicalLanguage.Syriac) TextDirection.Rtl else TextDirection.Ltr,
            color = LiturgicalColors.Black
        )

    @Composable
    fun withLanguageFont(base: TextStyle, language: LiturgicalLanguage): TextStyle =
        base.copy(fontFamily = LiturgicalFontFamilies.forLanguage(language))

    @Composable
    fun note(base: TextStyle, language: LiturgicalLanguage): TextStyle =
        withLanguageFont(base, language).copy(
            fontSize = base.fontSize * 1.15f,
            lineHeight = base.lineHeight * 1.15f,
            textDirection = if (language == LiturgicalLanguage.Arabic ||
                language == LiturgicalLanguage.Syriac) TextDirection.Rtl else TextDirection.Ltr
        )
}
