package org.syriacplatform.presentation.theme

/**
 * A color-only, UI-independent representation of liturgical text.
 *
 * Square brackets mark red spans and are removed from the displayed text.
 * Syriac full stop and ܗܠܠܘܝܗ (with or without Syriac vowel marks) are always red.
 * Unmatched or nested brackets are preserved literally rather than losing text.
 */
data class LiturgicalColorSpan(val start: Int, val endExclusive: Int)

data class ParsedLiturgicalText(
    val text: String,
    val redSpans: List<LiturgicalColorSpan>
)

object LiturgicalTextMarkup {
    private const val HALLELUJAH = "ܗܠܠܘܝܗ"
    private const val SYRIAC_FULL_STOP = '܀'

    private fun isSyriacMark(char: Char): Boolean = char in '\u0730'..'\u074A'

    private fun isSyriacLetter(char: Char): Boolean =
        char in '\u0710'..'\u072F' || char in '\u074D'..'\u074F'

    /** Match Hallelujah without discarding vowel marks or matching within longer words. */
    private fun hallelujahEnd(source: String, start: Int): Int? {
        if (start > 0 && (isSyriacLetter(source[start - 1]) ||
                    isSyriacMark(source[start - 1]))) return null

        var cursor = start
        for (letter in HALLELUJAH) {
            if (cursor >= source.length || source[cursor] != letter) return null
            cursor++
            while (cursor < source.length && isSyriacMark(source[cursor])) cursor++
        }
        if (cursor < source.length && isSyriacLetter(source[cursor])) return null
        return cursor
    }


    fun parse(source: String, allRed: Boolean = false): ParsedLiturgicalText {
        if (allRed) {
            return ParsedLiturgicalText(
                text = source,
                redSpans = if (source.isEmpty()) emptyList() else listOf(LiturgicalColorSpan(0, source.length))
            )
        }

        val output = StringBuilder()
        val red = mutableListOf<Boolean>()
        var index = 0

        fun append(value: String, isRed: Boolean) {
            output.append(value)
            repeat(value.length) { red.add(isRed) }
        }

        while (index < source.length) {
            if (source[index] == '[') {
                val close = source.indexOf(']', index + 1)
                if (close > index + 1) {
                    val inner = source.substring(index + 1, close)
                    if (inner.none { it == '[' }) {
                        append(inner, true)
                    } else {
                        append(source.substring(index, close + 1), false)
                    }
                    index = close + 1
                    continue
                }
            }
            val matchEnd = if (source[index] == HALLELUJAH[0]) {
                hallelujahEnd(source, index)
            } else null
            if (matchEnd != null) {
                append(source.substring(index, matchEnd), true)
                index = matchEnd
                continue
            }
            val char = source[index]
            append(char.toString(), char == SYRIAC_FULL_STOP)
            index++
        }

        val spans = mutableListOf<LiturgicalColorSpan>()
        var start = -1
        for (i in red.indices) {
            if (red[i] && start < 0) start = i
            if (!red[i] && start >= 0) {
                spans.add(LiturgicalColorSpan(start, i))
                start = -1
            }
        }
        if (start >= 0) spans.add(LiturgicalColorSpan(start, red.size))
        return ParsedLiturgicalText(output.toString(), spans)
    }
}
