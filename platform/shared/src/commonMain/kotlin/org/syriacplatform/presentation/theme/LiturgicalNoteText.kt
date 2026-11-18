package org.syriacplatform.presentation.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily

/**
 * A note is Arabic by default. Syriac runs keep their bundled Syriac font,
 * including Syriac combining marks, without changing font size or text.
 */
object LiturgicalNoteText {
    internal fun isSyriac(char: Char): Boolean =
        char in '\u0700'..'\u074F' || char in '\u0860'..'\u086F'

    @Composable
    fun annotated(source: String): AnnotatedString {
        val syriacFamily = LiturgicalFontFamilies.syriac()
        return withSyriacFont(source, syriacFamily)
    }

    fun withSyriacFont(source: String, syriacFamily: FontFamily): AnnotatedString =
        buildAnnotatedString {
            append(source)
            var start = -1
            for (i in source.indices) {
                val syriac = isSyriac(source[i]) ||
                    (source[i] in '\u0300'..'\u036F' && start >= 0)
                if (syriac && start < 0) start = i
                if (!syriac && start >= 0) {
                    addStyle(SpanStyle(fontFamily = syriacFamily), start, i)
                    start = -1
                }
            }
            if (start >= 0) addStyle(SpanStyle(fontFamily = syriacFamily), start, source.length)
        }
}
