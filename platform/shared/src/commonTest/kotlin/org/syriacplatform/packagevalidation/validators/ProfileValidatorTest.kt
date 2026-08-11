package org.syriacplatform.packagevalidation.validators

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.syriacplatform.packageformat.models.PackageProfile
import org.syriacplatform.packagevalidation.PackageValidationTestFixture.allCollectionsPresent
import org.syriacplatform.packagevalidation.PackageValidationTestFixture.packageWith
import org.syriacplatform.packagevalidation.PackageValidationTestFixture.validManifest
import org.syriacplatform.packagevalidation.ValidationSeverity

class ProfileValidatorTest {

    private val validator =
        ProfileValidator()

    @Test
    fun occasionProfileWithRequiredCollectionsPresentProducesNoIssues() {
        val packageData = packageWith(
            manifest =
                validManifest().copy(
                    profile = PackageProfile.OCCASION
                ),
            collectionPresence =
                allCollectionsPresent().copy(
                    qolos = false,
                    melodies = false,
                    qintos = false,
                    petgomos = false,
                    melodyQintoAssignments = false
                )
        )

        val issues = validator.validate(packageData)

        assertTrue(issues.isEmpty())
    }

    @Test
    fun occasionProfileMissingRequiredCollectionProducesFatalIssue() {
        val packageData = packageWith(
            manifest =
                validManifest().copy(
                    profile = PackageProfile.OCCASION
                ),
            collectionPresence =
                allCollectionsPresent().copy(
                    occasions = false
                )
        )

        val issues = validator.validate(packageData)

        assertEquals(
            1,
            issues.size
        )

        assertEquals(
            ValidationSeverity.FATAL,
            issues.single().severity
        )

        assertEquals(
            "collectionPresence.occasions",
            issues.single().location
        )
    }

    @Test
    fun shhimaProfileAllowsQintoCollectionsToBeAbsent() {
        val packageData = packageWith(
            manifest =
                validManifest().copy(
                    profile = PackageProfile.SHHIMA
                ),
            collectionPresence =
                allCollectionsPresent().copy(
                    occasions = false,
                    qolos = false,
                    melodies = false,
                    qintos = false,
                    petgomos = false,
                    melodyQintoAssignments = false
                )
        )

        val issues = validator.validate(packageData)

        assertTrue(issues.isEmpty())
    }

    @Test
    fun shhimaProfileMissingRequiredPrayerCollectionProducesFatalIssue() {
        val packageData = packageWith(
            manifest =
                validManifest().copy(
                    profile = PackageProfile.SHHIMA
                ),
            collectionPresence =
                allCollectionsPresent().copy(
                    prayers = false
                )
        )

        val issues = validator.validate(packageData)

        assertEquals(
            1,
            issues.size
        )

        assertEquals(
            ValidationSeverity.FATAL,
            issues.single().severity
        )

        assertEquals(
            "collectionPresence.prayers",
            issues.single().location
        )
    }

    @Test
    fun fullLibraryProfileWithAllCollectionsPresentProducesNoIssues() {
        val packageData = packageWith(
            manifest =
                validManifest().copy(
                    profile = PackageProfile.FULL_LIBRARY
                ),
            collectionPresence =
                allCollectionsPresent()
        )

        val issues = validator.validate(packageData)

        assertTrue(issues.isEmpty())
    }

    @Test
    fun fullLibraryRequiredCollectionMayBePresentButEmpty() {
        val packageData = packageWith(
            manifest =
                validManifest().copy(
                    profile = PackageProfile.FULL_LIBRARY
                ),
            collectionPresence =
                allCollectionsPresent(),
            petgomos = emptyList()
        )

        val issues = validator.validate(packageData)

        assertTrue(issues.isEmpty())
    }

    @Test
    fun fullLibraryMissingRequiredCollectionProducesFatalIssue() {
        val packageData = packageWith(
            manifest =
                validManifest().copy(
                    profile = PackageProfile.FULL_LIBRARY
                ),
            collectionPresence =
                allCollectionsPresent().copy(
                    petgomos = false
                ),
            petgomos = emptyList()
        )

        val issues = validator.validate(packageData)

        assertEquals(
            1,
            issues.size
        )

        assertEquals(
            ValidationSeverity.FATAL,
            issues.single().severity
        )

        assertEquals(
            "collectionPresence.petgomos",
            issues.single().location
        )
    }

    @Test
    fun multipleMissingRequiredCollectionsProduceMultipleFatalIssues() {
        val packageData = packageWith(
            manifest =
                validManifest().copy(
                    profile = PackageProfile.FULL_LIBRARY
                ),
            collectionPresence =
                allCollectionsPresent().copy(
                    qolos = false,
                    melodies = false,
                    petgomos = false
                )
        )

        val issues = validator.validate(packageData)

        assertEquals(
            3,
            issues.size
        )

        assertTrue(
            issues.all { issue ->
                issue.severity == ValidationSeverity.FATAL
            }
        )

        assertEquals(
            setOf(
                "collectionPresence.qolos",
                "collectionPresence.melodies",
                "collectionPresence.petgomos"
            ),
            issues
                .mapNotNull { issue ->
                    issue.location
                }
                .toSet()
        )
    }
}