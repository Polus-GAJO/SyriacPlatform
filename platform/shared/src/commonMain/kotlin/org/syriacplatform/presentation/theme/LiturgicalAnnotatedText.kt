package org.syriacplatform.presentation.theme

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle

/**
 * Converts the UI-independent liturgical markup into Compose text spans.
 *
 * Does not change typography or apply any style to existing screens until
 * a caller explicitly opts in.
 */
object LiturgicalAnnotatedText {
    fun fromSource(source: String, allRed: Boolean = false): AnnotatedString =
        fromParsed(LiturgicalTextMarkup.parse(source, allRed))

    fun fromParsed(parsed: ParsedLiturgicalText): AnnotatedString = buildAnnotatedString {
        // Preserve span offsets: replace breakable spaces before the Syriac full stop.
        val value = parsed.text.replace(Regex("[ \\t]+(?=܀)")) { match ->
            "\u00A0".repeat(match.value.length)
        }
        var cursor = 0
        for (span in parsed.redSpans) {
            require(span.start >= cursor && span.endExclusive <= value.length &&
                span.endExclusive > span.start) {
                "Red spans must be non-overlapping, ordered, and within the text"
            }
            append(value.substring(cursor, span.start))
            withStyle(SpanStyle(color = LiturgicalColors.Red)) {
                append(value.substring(span.start, span.endExclusive))
            }
            cursor = span.endExclusive
        }
        append(value.substring(cursor))
    }
}
