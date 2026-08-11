package org.syriacplatform.packageformat.parsed

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.syriacplatform.packagevalidation.PackageValidationTestFixture.allCollectionsPresent
import org.syriacplatform.packagevalidation.PackageValidationTestFixture.packageWith

class PackageCollectionPresenceTest {

    @Test
    fun presentButEmptyCollectionIsRepresentedAsPresent() {
        val packageData = packageWith(
            collectionPresence =
                allCollectionsPresent().copy(
                    petgomos = true
                ),
            petgomos = emptyList()
        )

        assertTrue(
            packageData.collectionPresence.petgomos
        )

        assertTrue(
            packageData.petgomos.isEmpty()
        )
    }

    @Test
    fun absentCollectionIsRepresentedAsAbsent() {
        val packageData = packageWith(
            collectionPresence =
                allCollectionsPresent().copy(
                    petgomos = false
                ),
            petgomos = emptyList()
        )

        assertFalse(
            packageData.collectionPresence.petgomos
        )

        assertTrue(
            packageData.petgomos.isEmpty()
        )
    }
}