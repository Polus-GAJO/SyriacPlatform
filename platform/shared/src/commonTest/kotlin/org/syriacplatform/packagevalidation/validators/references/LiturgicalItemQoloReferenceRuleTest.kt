package org.syriacplatform.packagevalidation.validators.references

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.syriacplatform.common.types.LiturgicalItemId
import org.syriacplatform.common.types.MelodyId
import org.syriacplatform.common.types.QoloId
import org.syriacplatform.content.models.LiturgicalItem
import org.syriacplatform.content.models.LiturgicalItemTarget
import org.syriacplatform.content.models.Melody
import org.syriacplatform.content.models.Qolo
import org.syriacplatform.packagevalidation.PackageValidationTestFixture.packageWith
import org.syriacplatform.packagevalidation.ValidationSeverity
import org.syriacplatform.common.types.GroupId

class LiturgicalItemQoloReferenceRuleTest {

    private val rule = LiturgicalItemQoloReferenceRule()

    @Test
    fun existingQoloAndMelodyReferencesProduceNoIssues() {
        val packageData = packageWith(
            liturgicalItems = listOf(
                LiturgicalItem(
                    id = LiturgicalItemId(501),
                    target = LiturgicalItemTarget.Qolo(
                        qoloId = QoloId(438),
                        effectiveMelodyId = MelodyId(75)
                    )
                )
            ),
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
            liturgicalItems = listOf(
                LiturgicalItem(
                    id = LiturgicalItemId(501),
                    target = LiturgicalItemTarget.Qolo(
                        qoloId = QoloId(999),
                        effectiveMelodyId = MelodyId(75)
                    )
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

        assertEquals(1, issues.size)

        assertEquals(
            ValidationSeverity.FATAL,
            issues.single().severity
        )

        assertEquals(
            "liturgicalItems[501].target.qoloId",
            issues.single().location
        )
    }

    @Test
    fun missingEffectiveMelodyReferenceProducesFatalIssue() {
        val packageData = packageWith(
            liturgicalItems = listOf(
                LiturgicalItem(
                    id = LiturgicalItemId(501),
                    target = LiturgicalItemTarget.Qolo(
                        qoloId = QoloId(438),
                        effectiveMelodyId = MelodyId(999)
                    )
                )
            ),
            qolos = listOf(
                Qolo(
                    id = QoloId(438),
                    groupId = GroupId(12),
                    sort = 500,
                    name = "Test Qolo",
                    searchName = "Test Qolo",
                    poeticMeter = null
                )
            )
        )

        val issues = rule.validate(packageData)

        assertEquals(1, issues.size)

        assertEquals(
            ValidationSeverity.FATAL,
            issues.single().severity
        )

        assertEquals(
            "liturgicalItems[501].target.effectiveMelodyId",
            issues.single().location
        )
    }

    @Test
    fun missingQoloAndMelodyReferencesProduceTwoFatalIssues() {
        val packageData = packageWith(
            liturgicalItems = listOf(
                LiturgicalItem(
                    id = LiturgicalItemId(501),
                    target = LiturgicalItemTarget.Qolo(
                        qoloId = QoloId(999),
                        effectiveMelodyId = MelodyId(888)
                    )
                )
            )
        )

        val issues = rule.validate(packageData)

        assertEquals(2, issues.size)

        assertTrue(
            issues.all { issue ->
                issue.severity == ValidationSeverity.FATAL
            }
        )

        assertEquals(
            setOf(
                "liturgicalItems[501].target.qoloId",
                "liturgicalItems[501].target.effectiveMelodyId"
            ),
            issues
                .mapNotNull { issue -> issue.location }
                .toSet()
        )
    }
}