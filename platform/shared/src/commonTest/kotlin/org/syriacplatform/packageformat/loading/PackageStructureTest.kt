package org.syriacplatform.packageformat.loading

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.syriacplatform.packageformat.parsed.PackageCollectionPresence

class PackageStructureTest {

    @Test
    fun structureKeepsManifestPresenceSeparateFromCollections() {
        val structure =
            PackageStructure(
                manifestPresent = true,
                collectionPresence =
                    PackageCollectionPresence(
                        entryPoints = true,
                        occasions = true,
                        prayers = true,
                        prayerSequences = true,
                        liturgicalItems = true,
                        texts = true,
                        qolos = true,
                        melodies = true,
                        qintos = true,
                        petgomos = false,
                        melodyQintoAssignments = false
                    )
            )

        assertTrue(
            structure.manifestPresent
        )

        assertFalse(
            structure.collectionPresence.petgomos
        )

        assertFalse(
            structure.collectionPresence.melodyQintoAssignments
        )
    }
}