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
            ParsedLiturgicalText("ܫܠܡܐ܀", listOf(LiturgicalColorSpan(5, 6))),
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
