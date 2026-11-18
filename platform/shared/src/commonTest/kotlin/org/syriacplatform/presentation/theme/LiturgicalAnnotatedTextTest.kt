package org.syriacplatform.presentation.theme

import kotlin.test.Test
import kotlin.test.assertEquals

class LiturgicalAnnotatedTextTest {
    @Test
    fun bracketedTextIsRedAndBracketsAreHidden() {
        val result = LiturgicalAnnotatedText.fromSource("ܐ [ܒܓ] ܕ")
        assertEquals("ܐ ܒܓ ܕ", result.text)
        assertEquals(1, result.spanStyles.size)
        assertEquals(2, result.spanStyles.single().start)
        assertEquals(4, result.spanStyles.single().end)
        assertEquals(LiturgicalColors.Red, result.spanStyles.single().item.color)
    }

    @Test
    fun fullStopAndHallelujahAreRed() {
        val result = LiturgicalAnnotatedText.fromSource("܀ ܗܠܠܘܝܗ")
        assertEquals(2, result.spanStyles.size)
        assertEquals(LiturgicalColors.Red, result.spanStyles[0].item.color)
        assertEquals(LiturgicalColors.Red, result.spanStyles[1].item.color)
    }

    @Test
    fun petgomoCanBeFullyRed() {
        val result = LiturgicalAnnotatedText.fromSource("ܫܠܡܐ ܀", allRed = true)
        assertEquals(1, result.spanStyles.size)
        assertEquals(0, result.spanStyles.single().start)
        assertEquals(result.length, result.spanStyles.single().end)
    }

    @Test
    fun plainTextHasNoExplicitRedStyle() {
        val result = LiturgicalAnnotatedText.fromSource("ܫܠܡܐ")
        assertEquals("ܫܠܡܐ", result.text)
        assertEquals(0, result.spanStyles.size)
    }
}
