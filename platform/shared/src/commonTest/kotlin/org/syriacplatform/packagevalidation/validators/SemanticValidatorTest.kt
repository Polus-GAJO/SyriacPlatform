package org.syriacplatform.packagevalidation.validators

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.syriacplatform.common.types.LiturgicalItemId
import org.syriacplatform.common.types.MelodyId
import org.syriacplatform.common.types.QoloId
import org.syriacplatform.content.models.LiturgicalItem
import org.syriacplatform.content.models.LiturgicalItemTarget
import org.syriacplatform.content.models.Melody
import org.syriacplatform.packagevalidation.PackageValidationTestFixture.packageWith
import org.syriacplatform.packagevalidation.ValidationSeverity

class SemanticValidatorTest {

    private val validator = SemanticValidator()

    @Test
    fun validSemanticPackageProducesNoIssues() {
        val packageData = packageWith(
            liturgicalItems = listOf(
                LiturgicalItem(
                    id = LiturgicalItemId(501),
                    target = LiturgicalItemTarget.Qolo(
                        qoloId = QoloId(438),
                        effectiveMelodyId = MelodyId(75),
                        verses = emptyList()
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

        val issues = validator.validate(packageData)

        assertTrue(issues.isEmpty())
    }

    @Test
    fun validatorCollectsEffectiveMelodySemanticIssue() {
        val packageData = packageWith(
            liturgicalItems = listOf(
                LiturgicalItem(
                    id = LiturgicalItemId(501),
                    target = LiturgicalItemTarget.Qolo(
                        qoloId = QoloId(438),
                        effectiveMelodyId = MelodyId(75),
                        verses = emptyList()
                    )
                )
            ),
            melodies = listOf(
                Melody(
                    id = MelodyId(75),
                    qoloId = QoloId(500),
                    name = "Wrong Qolo Melody",
                    searchName = "Wrong Qolo Melody",
                    hasRecording = false
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
            "liturgicalItems[501].target.effectiveMelodyId",
            issues.single().location
        )
    }

    @Test
    fun missingMelodyProducesNoSemanticIssue() {
        val packageData = packageWith(
            liturgicalItems = listOf(
                LiturgicalItem(
                    id = LiturgicalItemId(501),
                    target = LiturgicalItemTarget.Qolo(
                        qoloId = QoloId(438),
                        effectiveMelodyId = MelodyId(999),
                        verses = emptyList()
                    )
                )
            )
        )

        val issues = validator.validate(packageData)

        assertTrue(issues.isEmpty())
    }
}