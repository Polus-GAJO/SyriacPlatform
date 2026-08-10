package org.syriacplatform.packagevalidation.validators.references

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.syriacplatform.common.types.GroupId
import org.syriacplatform.common.types.MelodyId
import org.syriacplatform.common.types.QoloId
import org.syriacplatform.content.models.Melody
import org.syriacplatform.content.models.Qolo
import org.syriacplatform.packagevalidation.PackageValidationTestFixture.packageWith
import org.syriacplatform.packagevalidation.ValidationSeverity

class MelodyQoloReferenceRuleTest {

    private val rule = MelodyQoloReferenceRule()

    @Test
    fun existingQoloReferenceProducesNoIssues() {
        val packageData = packageWith(
            qolos = listOf(
                Qolo(
                    id = QoloId(438),
                    groupId = GroupId(12),
                    sort = 500,
                    name = "Test Qolo",
                    searchName = "Test Qolo",
                    poeticMeter = null
                )
            ),
            melodies = listOf(
                Melody(
                    id = MelodyId(75),
                    qoloId = QoloId(438),
                    name = "Test Melody",
                    searchName = "Test Melody",
                    hasRecording = false
                )
            )
        )

        val issues = rule.validate(packageData)

        assertTrue(issues.isEmpty())
    }

    @Test
    fun missingQoloReferenceProducesFatalIssue() {
        val packageData = packageWith(
            melodies = listOf(
                Melody(
                    id = MelodyId(75),
                    qoloId = QoloId(999),
                    name = "Test Melody",
                    searchName = "Test Melody",
                    hasRecording = false
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
            "melodies[75].qoloId",
            issues.single().location
        )
    }
}