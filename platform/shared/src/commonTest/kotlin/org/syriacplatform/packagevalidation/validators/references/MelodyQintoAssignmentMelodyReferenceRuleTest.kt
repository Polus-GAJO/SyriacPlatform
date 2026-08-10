package org.syriacplatform.packagevalidation.validators.references

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.syriacplatform.common.types.MelodyId
import org.syriacplatform.common.types.QintoId
import org.syriacplatform.common.types.QoloId
import org.syriacplatform.content.models.Melody
import org.syriacplatform.content.models.MelodyQintoAssignment
import org.syriacplatform.packagevalidation.PackageValidationTestFixture.packageWith
import org.syriacplatform.packagevalidation.ValidationSeverity

class MelodyQintoAssignmentMelodyReferenceRuleTest {

    private val rule =
        MelodyQintoAssignmentMelodyReferenceRule()

    @Test
    fun existingMelodyReferenceProducesNoIssues() {
        val packageData = packageWith(
            melodies = listOf(
                Melody(
                    id = MelodyId(75),
                    qoloId = QoloId(438),
                    name = "Test Melody",
                    searchName = "Test Melody",
                    hasRecording = false
                )
            ),
            melodyQintoAssignments = listOf(
                MelodyQintoAssignment(
                    melodyId = MelodyId(75),
                    qintoId = QintoId(4),
                    role = null
                )
            )
        )

        val issues = rule.validate(packageData)

        assertTrue(issues.isEmpty())
    }

    @Test
    fun missingMelodyReferenceProducesFatalIssue() {
        val packageData = packageWith(
            melodyQintoAssignments = listOf(
                MelodyQintoAssignment(
                    melodyId = MelodyId(999),
                    qintoId = QintoId(4),
                    role = null
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
            "melodyQintoAssignments[0].melodyId",
            issues.single().location
        )
    }
}