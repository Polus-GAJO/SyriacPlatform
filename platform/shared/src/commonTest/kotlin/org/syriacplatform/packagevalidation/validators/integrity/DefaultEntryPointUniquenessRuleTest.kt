package org.syriacplatform.packagevalidation.validators.integrity

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.syriacplatform.common.types.EntryPointId
import org.syriacplatform.common.types.OccasionId
import org.syriacplatform.content.models.EntryPoint
import org.syriacplatform.content.models.EntryPointTarget
import org.syriacplatform.packagevalidation.PackageValidationTestFixture.packageWith
import org.syriacplatform.packagevalidation.ValidationSeverity

class DefaultEntryPointUniquenessRuleTest {

    private val rule =
        DefaultEntryPointUniquenessRule()

    @Test
    fun noDefaultEntryPointProducesNoIssues() {
        val packageData = packageWith(
            entryPoints = listOf(
                EntryPoint(
                    id = EntryPointId(101),
                    name = "Entry Point 101",
                    target = EntryPointTarget.Occasion(
                        occasionId = OccasionId(201)
                    ),
                    isDefault = false
                ),
                EntryPoint(
                    id = EntryPointId(102),
                    name = "Entry Point 102",
                    target = EntryPointTarget.Occasion(
                        occasionId = OccasionId(202)
                    ),
                    isDefault = false
                )
            )
        )

        val issues = rule.validate(packageData)

        assertTrue(issues.isEmpty())
    }

    @Test
    fun oneDefaultEntryPointProducesNoIssues() {
        val packageData = packageWith(
            entryPoints = listOf(
                EntryPoint(
                    id = EntryPointId(101),
                    name = "Entry Point 101",
                    target = EntryPointTarget.Occasion(
                        occasionId = OccasionId(201)
                    ),
                    isDefault = true
                ),
                EntryPoint(
                    id = EntryPointId(102),
                    name = "Entry Point 102",
                    target = EntryPointTarget.Occasion(
                        occasionId = OccasionId(202)
                    ),
                    isDefault = false
                )
            )
        )

        val issues = rule.validate(packageData)

        assertTrue(issues.isEmpty())
    }

    @Test
    fun multipleDefaultEntryPointsProduceFatalIssue() {
        val packageData = packageWith(
            entryPoints = listOf(
                EntryPoint(
                    id = EntryPointId(101),
                    name = "Entry Point 101",
                    target = EntryPointTarget.Occasion(
                        occasionId = OccasionId(201)
                    ),
                    isDefault = true
                ),
                EntryPoint(
                    id = EntryPointId(102),
                    name = "Entry Point 102",
                    target = EntryPointTarget.Occasion(
                        occasionId = OccasionId(202)
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
            "entryPoints.isDefault",
            issues.single().location
        )
    }
}