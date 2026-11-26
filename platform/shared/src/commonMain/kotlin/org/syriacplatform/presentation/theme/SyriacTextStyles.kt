package org.syriacplatform.presentation.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.sp

object SyriacTextStyles {
    @Composable
    fun reading(text: String): TextStyle {
        val hasSyriac = text.any { it in '\u0700'..'\u074F' || it in '\u0860'..'\u086F' }
        val hasArabic = text.any { it in '\u0600'..'\u06FF' || it in '\u0750'..'\u077F' || it in '\u08A0'..'\u08FF' }
        return if (hasArabic && !hasSyriac) body().copy(
            fontFamily = LiturgicalFontFamilies.arabic(),
            fontSize = (LiturgicalTypography.SyriacBaseSp * 0.85f).sp,
            lineHeight = (LiturgicalTypography.SyriacBaseSp * 0.85f * 1.35f).sp
        ) else body()
    }

    @Composable
    fun body(): TextStyle = TextStyle(
        fontFamily = LiturgicalFontFamilies.syriac(),
        fontSize = LiturgicalTypography.SyriacBaseSp.sp,
        lineHeight = (LiturgicalTypography.SyriacBaseSp * 1.35f).sp,
        textAlign = TextAlign.Right,
        textDirection = TextDirection.Rtl
    )
}
