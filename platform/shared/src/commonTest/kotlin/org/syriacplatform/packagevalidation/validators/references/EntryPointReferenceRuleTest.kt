package org.syriacplatform.packagevalidation.validators.references

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.syriacplatform.common.types.EntryPointId
import org.syriacplatform.common.types.OccasionId
import org.syriacplatform.content.models.EntryPoint
import org.syriacplatform.content.models.EntryPointTarget
import org.syriacplatform.content.models.Occasion
import org.syriacplatform.packagevalidation.PackageValidationTestFixture.packageWith
import org.syriacplatform.packagevalidation.ValidationSeverity

class EntryPointReferenceRuleTest {

    private val rule = EntryPointReferenceRule()

    @Test
    fun existingEntryPointOccasionReferenceProducesNoIssues() {
        val packageData = packageWith(
            entryPoints = listOf(
                EntryPoint(
                    id = EntryPointId(1),
                    name = "Nativity",
                    target = EntryPointTarget.Occasion(
                        occasionId = OccasionId(101)
                    ),
                    isDefault = true
                )
            ),
            occasions = listOf(
                Occasion(
                    id = OccasionId(101),
                    name = "Nativity",
                    description = null,
                    prayerSequenceIds = emptyList()
                )
            )
        )

        val issues = rule.validate(packageData)

        assertTrue(issues.isEmpty())
    }

    @Test
    fun missingEntryPointOccasionReferenceProducesFatalIssue() {
        val packageData = packageWith(
            entryPoints = listOf(
                EntryPoint(
                    id = EntryPointId(1),
                    name = "Nativity",
                    target = EntryPointTarget.Occasion(
                        occasionId = OccasionId(999)
                    ),
                    isDefault = true
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
            "entryPoints[1].target",
            issues.single().location
        )
    }
}