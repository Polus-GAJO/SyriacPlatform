package org.syriacplatform.presentation.theme

import kotlin.test.Test
import kotlin.test.assertEquals

class LiturgicalTextMarkupTest {
    @Test
    fun plainTextRemainsBlack() {
        assertEquals(ParsedLiturgicalText("ܫܠܡܐ", emptyList()), LiturgicalTextMarkup.parse("ܫܠܡܐ"))
    }

    @Test
    fun bracketedTextBecomesRedWithoutBrackets() {
        val result = LiturgicalTextMarkup.parse("ܫܠܡܐ [ܬܠܬ] ܐܡܝܢ")
        assertEquals("ܫܠܡܐ ܬܠܬ ܐܡܝܢ", result.text)
        val start = result.text.indexOf("ܬܠܬ")
        assertEquals(listOf(LiturgicalColorSpan(start, start + "ܬܠܬ".length)), result.redSpans)
    }

    @Test
    fun syriacFullStopIsAlwaysRed() {
        assertEquals(
            ParsedLiturgicalText("ܫܠܡܐ܀", listOf(LiturgicalColorSpan(4, 5))),
            LiturgicalTextMarkup.parse("ܫܠܡܐ܀")
        )
    }

    @Test
    fun hallelujahIsAlwaysRed() {
        val result = LiturgicalTextMarkup.parse("ܐܡܝܢ ܗܠܠܘܝܗ")
        val start = result.text.indexOf("ܗܠܠܘܝܗ")
        assertEquals(listOf(LiturgicalColorSpan(start, result.text.length)), result.redSpans)
    }


    @Test
    fun vocalizedHallelujahIsFullyRedWithoutChangingItsMarks() {
        val word = "ܗܰܠܶܠܽܘܝܰܗ"
        val source = "ܐܡܝܢ $word ܫܠܡܐ"
        val result = LiturgicalTextMarkup.parse(source)
        assertEquals(source, result.text)
        val start = source.indexOf(word)
        assertEquals(listOf(LiturgicalColorSpan(start, start + word.length)), result.redSpans)
    }

    @Test
    fun hallelujahIsNotMatchedInsideLongerSyriacWord() {
        assertEquals(emptyList(), LiturgicalTextMarkup.parse("ܒܗܰܠܶܠܽܘܝܰܗܐ").redSpans)
    }

    @Test
    fun unvocalizedHallelujahStillMatches() {
        val word = "ܗܠܠܘܝܗ"
        assertEquals(
            listOf(LiturgicalColorSpan(0, word.length)),
            LiturgicalTextMarkup.parse(word).redSpans
        )
    }

    @Test
    fun adjoiningRedRegionsMerge() {
        assertEquals(
            ParsedLiturgicalText("܀ܗܠܠܘܝܗ", listOf(LiturgicalColorSpan(0, "܀ܗܠܠܘܝܗ".length))),
            LiturgicalTextMarkup.parse("܀[ܗܠܠܘܝܗ]")
        )
    }

    @Test
    fun petgomoIsEntirelyRedAndPreservesOriginalText() {
        assertEquals(
            ParsedLiturgicalText("[ܫܠܡܐ] ܀", listOf(LiturgicalColorSpan(0, "[ܫܠܡܐ] ܀".length))),
            LiturgicalTextMarkup.parse("[ܫܠܡܐ] ܀", allRed = true)
        )
    }

    @Test
    fun malformedBracketsRemainVisible() {
        assertEquals("[ܫܠܡܐ", LiturgicalTextMarkup.parse("[ܫܠܡܐ").text)
        assertEquals("[[ܫܠܡܐ]", LiturgicalTextMarkup.parse("[[ܫܠܡܐ]").text)
    }

    @Test
    fun emptyInputProducesNoSpans() {
        assertEquals(ParsedLiturgicalText("", emptyList()), LiturgicalTextMarkup.parse(""))
    }
}
