package org.syriacplatform.presentation.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import org.jetbrains.compose.resources.Font
import org.syriacplatform.resources.Res
import org.syriacplatform.resources.serto_jerusalem
import androidx.compose.ui.unit.sp

object SyriacTextStyles {

    @Composable
    fun body(): TextStyle {
        return TextStyle(
            fontFamily =
                FontFamily(
                    Font(
                        Res.font.serto_jerusalem
                    )
                ),
            fontSize = 26.sp,
            textAlign = TextAlign.Right
        )
    }
}