package org.syriacplatform.packagevalidation.validators

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.syriacplatform.common.types.TextId
import org.syriacplatform.content.models.TextContent
import org.syriacplatform.packagevalidation.PackageValidationTestFixture.packageWith
import org.syriacplatform.packagevalidation.ValidationSeverity
import org.syriacplatform.common.types.EntryPointId
import org.syriacplatform.common.types.OccasionId
import org.syriacplatform.content.models.EntryPoint
import org.syriacplatform.content.models.EntryPointTarget
import org.syriacplatform.common.types.MelodyId
import org.syriacplatform.common.types.QintoId
import org.syriacplatform.content.models.MelodyQintoAssignment

class IntegrityValidatorTest {

    private val validator = IntegrityValidator()

    @Test
    fun validPackageProducesNoIntegrityIssues() {
        val packageData = packageWith(
            texts = listOf(
                TextContent(
                    id = TextId(25),
                    syriac = "Test text",
                    translations = emptyList()
                )
            )
        )

        val issues = validator.validate(packageData)

        assertTrue(issues.isEmpty())
    }

    @Test
    fun validatorCollectsCanonicalIdUniquenessIssue() {
        val packageData = packageWith(
            texts = listOf(
                TextContent(
                    id = TextId(25),
                    syriac = "First definition",
                    translations = emptyList()
                ),
                TextContent(
                    id = TextId(25),
                    syriac = "Second definition",
                    translations = emptyList()
                )
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
            "texts[id=25]",
            issues.single().location
        )
    }

    @Test
    fun validatorCollectsDefaultEntryPointUniquenessIssue() {
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
            "entryPoints.isDefault",
            issues.single().location
        )
    }

    @Test
    fun validatorCollectsIssuesFromMultipleIntegrityRules() {
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
            ),
            melodyQintoAssignments = listOf(
                MelodyQintoAssignment(
                    melodyId = MelodyId(75),
                    qintoId = QintoId(4),
                    role = null
                ),
                MelodyQintoAssignment(
                    melodyId = MelodyId(75),
                    qintoId = QintoId(4),
                    role = null
                )
            ),
            texts = listOf(
                TextContent(
                    id = TextId(25),
                    syriac = "First definition",
                    translations = emptyList()
                ),
                TextContent(
                    id = TextId(25),
                    syriac = "Second definition",
                    translations = emptyList()
                )
            )
        )

        val issues = validator.validate(packageData)

        assertEquals(
            3,
            issues.size
        )

        assertEquals(
            setOf(
                "texts[id=25]",
                "entryPoints.isDefault",
                "melodyQintoAssignments[melodyId=75,qintoId=4]"
            ),
            issues
                .mapNotNull { issue ->
                    issue.location
                }
                .toSet()
        )
    }

    @Test
    fun validatorCollectsMelodyQintoAssignmentUniquenessIssue() {
        val packageData = packageWith(
            melodyQintoAssignments = listOf(
                MelodyQintoAssignment(
                    melodyId = MelodyId(75),
                    qintoId = QintoId(4),
                    role = null
                ),
                MelodyQintoAssignment(
                    melodyId = MelodyId(75),
                    qintoId = QintoId(4),
                    role = null
                )
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
            "melodyQintoAssignments[melodyId=75,qintoId=4]",
            issues.single().location
        )
    }
}