package org.syriacplatform.packagevalidation.validators.references

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.syriacplatform.common.types.OccasionId
import org.syriacplatform.common.types.PrayerId
import org.syriacplatform.common.types.PrayerSequenceId
import org.syriacplatform.content.models.Occasion
import org.syriacplatform.content.models.Prayer
import org.syriacplatform.content.models.PrayerSequence
import org.syriacplatform.packagevalidation.PackageValidationTestFixture.packageWith
import org.syriacplatform.packagevalidation.ValidationSeverity

class OccasionReferenceRuleTest {

    private val rule = OccasionReferenceRule()

    @Test
    fun existingOccasionPrayerSequenceReferencesProduceNoIssues() {
        val packageData = packageWith(
            occasions = listOf(
                Occasion(
                    id = OccasionId(101),
                    name = "Nativity",
                    description = null,
                    prayerSequenceIds = listOf(
                        PrayerSequenceId(301),
                        PrayerSequenceId(302)
                    )
                )
            ),
            prayers = listOf(
                Prayer(
                    id = PrayerId(201),
                    name = "Evening Prayer",
                    description = null
                ),
                Prayer(
                    id = PrayerId(202),
                    name = "Morning Prayer",
                    description = null
                )
            ),
            prayerSequences = listOf(
                PrayerSequence(
                    id = PrayerSequenceId(301),
                    prayerId = PrayerId(201),
                    liturgicalItemIds = emptyList()
                ),
                PrayerSequence(
                    id = PrayerSequenceId(302),
                    prayerId = PrayerId(202),
                    liturgicalItemIds = emptyList()
                )
            )
        )

        val issues = rule.validate(packageData)

        assertTrue(issues.isEmpty())
    }

    @Test
    fun missingOccasionPrayerSequenceReferenceProducesFatalIssue() {
        val packageData = packageWith(
            occasions = listOf(
                Occasion(
                    id = OccasionId(101),
                    name = "Nativity",
                    description = null,
                    prayerSequenceIds = listOf(
                        PrayerSequenceId(301),
                        PrayerSequenceId(999)
                    )
                )
            ),
            prayers = listOf(
                Prayer(
                    id = PrayerId(201),
                    name = "Evening Prayer",
                    description = null
                )
            ),
            prayerSequences = listOf(
                PrayerSequence(
                    id = PrayerSequenceId(301),
                    prayerId = PrayerId(201),
                    liturgicalItemIds = emptyList()
                )
            )
        )

        val issues = rule.validate(packageData)

        assertEquals(
            1,
            issues.size
        )

        assertEquals(
            ValidationSeverity.FATAL,
            issues.single().severity
        )

        assertEquals(
            "occasions[101].prayerSequenceIds[1]",
            issues.single().location
        )
    }
}