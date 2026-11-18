package org.syriacplatform.presentation.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import org.jetbrains.compose.resources.Font
import org.syriacplatform.resources.Res
import org.syriacplatform.resources.serto_jerusalem

/**
 * Font-family registry for liturgical content.
 *
 * Syriac is connected to the existing bundled Serto Jerusalem resource.
 * Arabic (Traditional Arabic) and Latin (Noto Sans) must not silently use
 * platform fallbacks: add and verify their font resources before enabling
 * them here. This keeps the existing UI unchanged.
 */
object LiturgicalFontFamilies {
    @Composable
    fun syriac(): FontFamily = FontFamily(Font(Res.font.serto_jerusalem))
}
