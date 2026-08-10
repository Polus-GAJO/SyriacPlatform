package org.syriacplatform.packagevalidation.validators.integrity

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.syriacplatform.common.types.MelodyId
import org.syriacplatform.common.types.QintoId
import org.syriacplatform.content.models.MelodyQintoAssignment
import org.syriacplatform.packagevalidation.PackageValidationTestFixture.packageWith
import org.syriacplatform.packagevalidation.ValidationSeverity

class MelodyQintoAssignmentUniquenessRuleTest {

    private val rule =
        MelodyQintoAssignmentUniquenessRule()

    @Test
    fun noAssignmentsProduceNoIssues() {
        val packageData = packageWith()

        val issues = rule.validate(packageData)

        assertTrue(issues.isEmpty())
    }

    @Test
    fun differentMelodyQintoAssignmentsProduceNoIssues() {
        val packageData = packageWith(
            melodyQintoAssignments = listOf(
                MelodyQintoAssignment(
                    melodyId = MelodyId(75),
                    qintoId = QintoId(4),
                    role = null
                ),
                MelodyQintoAssignment(
                    melodyId = MelodyId(75),
                    qintoId = QintoId(5),
                    role = null
                ),
                MelodyQintoAssignment(
                    melodyId = MelodyId(76),
                    qintoId = QintoId(4),
                    role = null
                )
            )
        )

        val issues = rule.validate(packageData)

        assertTrue(issues.isEmpty())
    }

    @Test
    fun duplicateMelodyQintoAssignmentProducesFatalIssue() {
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
            "melodyQintoAssignments[melodyId=75,qintoId=4]",
            issues.single().location
        )
    }

    @Test
    fun sameAssignmentRepeatedThreeTimesProducesOneFatalIssue() {
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
                ),
                MelodyQintoAssignment(
                    melodyId = MelodyId(75),
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
            "melodyQintoAssignments[melodyId=75,qintoId=4]",
            issues.single().location
        )
    }
}