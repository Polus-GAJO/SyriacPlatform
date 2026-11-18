package org.syriacplatform.presentation.theme

import androidx.compose.ui.text.font.FontFamily
import kotlin.test.Test
import kotlin.test.assertEquals

class LiturgicalNoteTextTest {
    @Test
    fun arabicNoteKeepsTextAndHasNoSyriacSpans() {
        val source = "هذه ملاحظة عربية."
        val result = LiturgicalNoteText.withSyriacFont(source, FontFamily.Serif)
        assertEquals(source, result.text)
        assertEquals(emptyList(), result.spanStyles)
    }

    @Test
    fun mixedNoteStylesOnlySyriacRuns() {
        val source = "نقرأ ܫܠܡܐ ثم ܗܰܠܶܠܽܘܝܰܗ."
        val result = LiturgicalNoteText.withSyriacFont(source, FontFamily.Serif)
        assertEquals(source, result.text)
        assertEquals(2, result.spanStyles.size)
        for ((index, word) in listOf("ܫܠܡܐ", "ܗܰܠܶܠܽܘܝܰܗ").withIndex()) {
            val span = result.spanStyles[index]
            assertEquals(source.indexOf(word), span.start)
            assertEquals(source.indexOf(word) + word.length, span.end)
            assertEquals(FontFamily.Serif, span.item.fontFamily)
        }
    }

    @Test
    fun syriacAtEndOfNoteIsStyled() {
        val source = "النص ܫܠܡܐ"
        val result = LiturgicalNoteText.withSyriacFont(source, FontFamily.Serif)
        assertEquals(source.length, result.spanStyles.single().end)
    }
}
