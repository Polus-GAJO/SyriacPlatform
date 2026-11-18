package org.syriacplatform.presentation.theme

/**
 * A color-only, UI-independent representation of liturgical text.
 *
 * Square brackets mark red spans and are removed from the displayed text.
 * Syriac full stop and the exact word ܗܠܠܘܝܗ are always red.
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
            if (source.startsWith(HALLELUJAH, index)) {
                append(HALLELUJAH, true)
                index += HALLELUJAH.length
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
