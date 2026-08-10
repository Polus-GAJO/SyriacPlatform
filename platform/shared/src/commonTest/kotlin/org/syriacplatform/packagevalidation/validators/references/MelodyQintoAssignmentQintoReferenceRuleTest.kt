package org.syriacplatform.packagevalidation.validators.references

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.syriacplatform.common.types.MelodyId
import org.syriacplatform.common.types.QintoId
import org.syriacplatform.content.models.MelodyQintoAssignment
import org.syriacplatform.content.models.Qinto
import org.syriacplatform.packagevalidation.PackageValidationTestFixture.packageWith
import org.syriacplatform.packagevalidation.ValidationSeverity

class MelodyQintoAssignmentQintoReferenceRuleTest {

    private val rule =
        MelodyQintoAssignmentQintoReferenceRule()

    @Test
    fun existingQintoReferenceProducesNoIssues() {
        val packageData = packageWith(
            qintos = listOf(
                Qinto(
                    id = QintoId(4),
                    name = "Test Qinto"
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
    fun missingQintoReferenceProducesFatalIssue() {
        val packageData = packageWith(
            melodyQintoAssignments = listOf(
                MelodyQintoAssignment(
                    melodyId = MelodyId(75),
                    qintoId = QintoId(999),
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
            "melodyQintoAssignments[0].qintoId",
            issues.single().location
        )
    }
}