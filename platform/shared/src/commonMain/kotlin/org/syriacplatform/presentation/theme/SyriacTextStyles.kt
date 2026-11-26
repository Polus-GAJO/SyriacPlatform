package org.syriacplatform.presentation.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.sp

object SyriacTextStyles {
    @Composable
    fun body(): TextStyle = TextStyle(
        fontFamily = LiturgicalFontFamilies.syriac(),
        fontSize = LiturgicalTypography.SyriacBaseSp.sp,
        lineHeight = (LiturgicalTypography.SyriacBaseSp * 1.35f).sp,
        textAlign = TextAlign.Right,
        textDirection = TextDirection.Rtl
    )
}
