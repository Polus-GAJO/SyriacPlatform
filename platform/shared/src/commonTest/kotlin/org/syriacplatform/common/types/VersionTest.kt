package org.syriacplatform.common.types

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class VersionTest {

    @Test
    fun toStringProducesSemanticVersionFormat() {
        val version =
            Version(
                major = 1,
                minor = 2,
                patch = 3
            )

        assertEquals(
            "1.2.3",
            version.toString()
        )
    }

    @Test
    fun parseOrNullParsesValidVersion() {
        val version =
            Version.parseOrNull("1.2.3")

        assertEquals(
            Version(
                major = 1,
                minor = 2,
                patch = 3
            ),
            version
        )
    }

    @Test
    fun parseOrNullIgnoresSurroundingWhitespace() {
        val version =
            Version.parseOrNull("  1.2.3  ")

        assertEquals(
            Version(
                major = 1,
                minor = 2,
                patch = 3
            ),
            version
        )
    }

    @Test
    fun parseOrNullRejectsInvalidFormats() {
        assertNull(
            Version.parseOrNull("1.2")
        )

        assertNull(
            Version.parseOrNull("1.2.3.4")
        )

        assertNull(
            Version.parseOrNull("abc")
        )

        assertNull(
            Version.parseOrNull("1.a.3")
        )

        assertNull(
            Version.parseOrNull("1.-2.3")
        )
    }

    @Test
    fun majorVersionHasHighestComparisonPriority() {
        assertTrue(
            Version(2, 0, 0) >
                    Version(1, 99, 99)
        )
    }

    @Test
    fun minorVersionIsComparedWhenMajorVersionsMatch() {
        assertTrue(
            Version(1, 3, 0) >
                    Version(1, 2, 99)
        )
    }

    @Test
    fun patchVersionIsComparedWhenMajorAndMinorVersionsMatch() {
        assertTrue(
            Version(1, 2, 4) >
                    Version(1, 2, 3)
        )
    }

    @Test
    fun multiDigitVersionNumbersAreComparedNumerically() {
        assertTrue(
            Version(1, 10, 0) >
                    Version(1, 2, 0)
        )
    }

    @Test
    fun equalVersionsCompareAsEqual() {
        val first =
            Version(1, 2, 3)

        val second =
            Version(1, 2, 3)

        assertEquals(
            0,
            first.compareTo(second)
        )
    }
}