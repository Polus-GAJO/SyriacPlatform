package org.syriacplatform.packagevalidation.validators.references

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.syriacplatform.common.types.PrayerId
import org.syriacplatform.common.types.PrayerSequenceId
import org.syriacplatform.content.models.Prayer
import org.syriacplatform.content.models.PrayerSequence
import org.syriacplatform.packagevalidation.PackageValidationTestFixture.packageWith
import org.syriacplatform.packagevalidation.ValidationSeverity

class PrayerSequenceReferenceRuleTest {

    private val rule = PrayerSequenceReferenceRule()

    @Test
    fun existingPrayerSequencePrayerReferenceProducesNoIssues() {
        val packageData = packageWith(
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

        assertTrue(issues.isEmpty())
    }

    @Test
    fun missingPrayerSequencePrayerReferenceProducesFatalIssue() {
        val packageData = packageWith(
            prayerSequences = listOf(
                PrayerSequence(
                    id = PrayerSequenceId(301),
                    prayerId = PrayerId(999),
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
            "prayerSequences[301].prayerId",
            issues.single().location
        )
    }
}