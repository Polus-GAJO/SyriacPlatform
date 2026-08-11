package org.syriacplatform.packageformat.loading

import kotlin.test.Test
import kotlin.test.assertEquals

class PackagePathsTest {

    @Test
    fun canonicalPathsMatchSchemaV1Specification() {
        assertEquals(
            "manifest.json",
            PackagePaths.MANIFEST
        )

        assertEquals(
            "content/entry-points.json",
            PackagePaths.ENTRY_POINTS
        )

        assertEquals(
            "content/occasions.json",
            PackagePaths.OCCASIONS
        )

        assertEquals(
            "content/prayers.json",
            PackagePaths.PRAYERS
        )

        assertEquals(
            "content/prayer-sequences.json",
            PackagePaths.PRAYER_SEQUENCES
        )

        assertEquals(
            "content/liturgical-items.json",
            PackagePaths.LITURGICAL_ITEMS
        )

        assertEquals(
            "content/texts.json",
            PackagePaths.TEXTS
        )

        assertEquals(
            "content/petgomos.json",
            PackagePaths.PETGOMOS
        )

        assertEquals(
            "content/qolos.json",
            PackagePaths.QOLOS
        )

        assertEquals(
            "content/melodies.json",
            PackagePaths.MELODIES
        )

        assertEquals(
            "content/qintos.json",
            PackagePaths.QINTOS
        )

        assertEquals(
            "content/melody-qinto-assignments.json",
            PackagePaths.MELODY_QINTO_ASSIGNMENTS
        )
    }
}