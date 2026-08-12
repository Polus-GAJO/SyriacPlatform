package org.syriacplatform.packagevalidation.validators.references

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.syriacplatform.common.types.LiturgicalItemId
import org.syriacplatform.common.types.PetgomoId
import org.syriacplatform.common.types.TextId
import org.syriacplatform.content.models.LiturgicalItem
import org.syriacplatform.content.models.LiturgicalItemTarget
import org.syriacplatform.content.models.Petgomo
import org.syriacplatform.packagevalidation.PackageValidationTestFixture.packageWith
import org.syriacplatform.packagevalidation.ValidationSeverity
import org.syriacplatform.common.types.MelodyId
import org.syriacplatform.common.types.QoloId
import org.syriacplatform.content.models.LiturgicalTextRef

class LiturgicalItemPetgomoReferenceRuleTest {

    private val rule =
        LiturgicalItemPetgomoReferenceRule()

    @Test
    fun textWithoutPetgomoReferenceProducesNoIssues() {
        val packageData = packageWith(
            liturgicalItems = listOf(
                LiturgicalItem(
                    id = LiturgicalItemId(501),
                    target = LiturgicalItemTarget.Text(
                        textId = TextId(1001),
                        petgomoId = null
                    )
                )
            )
        )

        val issues = rule.validate(packageData)

        assertTrue(issues.isEmpty())
    }

    @Test
    fun existingPetgomoReferenceProducesNoIssues() {
        val packageData = packageWith(
            liturgicalItems = listOf(
                LiturgicalItem(
                    id = LiturgicalItemId(501),
                    target = LiturgicalItemTarget.Text(
                        textId = TextId(1001),
                        petgomoId = PetgomoId(15)
                    )
                )
            ),
            petgomos = listOf(
                Petgomo(
                    id = PetgomoId(15),
                    syriac = "ܦܶܬܓܳܡܳܐ",
                    translations = emptyList()
                )
            )
        )

        val issues = rule.validate(packageData)

        assertTrue(issues.isEmpty())
    }

    @Test
    fun missingPetgomoReferenceProducesFatalIssue() {
        val packageData = packageWith(
            liturgicalItems = listOf(
                LiturgicalItem(
                    id = LiturgicalItemId(501),
                    target = LiturgicalItemTarget.Text(
                        textId = TextId(1001),
                        petgomoId = PetgomoId(999)
                    )
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
            "liturgicalItems[501].target.petgomoId",
            issues.single().location
        )
    }

    @Test
    fun qoloVerseWithoutPetgomoProducesNoIssues() {
        val packageData =
            packageWith(
                liturgicalItems = listOf(
                    LiturgicalItem(
                        id = LiturgicalItemId(501),
                        target =
                            LiturgicalItemTarget.Qolo(
                                qoloId = QoloId(438),
                                effectiveMelodyId =
                                    MelodyId(75),
                                verses = listOf(
                                    LiturgicalTextRef(
                                        textId =
                                            TextId(1001),
                                        petgomoId = null
                                    )
                                )
                            )
                    )
                )
            )

        val issues =
            rule.validate(packageData)

        assertTrue(
            issues.isEmpty()
        )
    }

    @Test
    fun existingQoloVersePetgomoReferenceProducesNoIssues() {
        val packageData =
            packageWith(
                liturgicalItems = listOf(
                    LiturgicalItem(
                        id = LiturgicalItemId(501),
                        target =
                            LiturgicalItemTarget.Qolo(
                                qoloId = QoloId(438),
                                effectiveMelodyId =
                                    MelodyId(75),
                                verses = listOf(
                                    LiturgicalTextRef(
                                        textId =
                                            TextId(1001),
                                        petgomoId =
                                            PetgomoId(15)
                                    )
                                )
                            )
                    )
                ),
                petgomos = listOf(
                    Petgomo(
                        id = PetgomoId(15),
                        syriac = "ܦܶܬܓܳܡܳܐ",
                        translations =
                            emptyList()
                    )
                )
            )

        val issues =
            rule.validate(packageData)

        assertTrue(
            issues.isEmpty()
        )
    }

    @Test
    fun missingQoloVersePetgomoReferenceProducesFatalIssue() {
        val packageData =
            packageWith(
                liturgicalItems = listOf(
                    LiturgicalItem(
                        id = LiturgicalItemId(501),
                        target =
                            LiturgicalItemTarget.Qolo(
                                qoloId = QoloId(438),
                                effectiveMelodyId =
                                    MelodyId(75),
                                verses = listOf(
                                    LiturgicalTextRef(
                                        textId =
                                            TextId(1001),
                                        petgomoId =
                                            PetgomoId(999)
                                    )
                                )
                            )
                    )
                )
            )

        val issues =
            rule.validate(packageData)

        assertEquals(
            1,
            issues.size
        )

        assertEquals(
            ValidationSeverity.FATAL,
            issues.single().severity
        )

        assertEquals(
            "liturgicalItems[501].target.verses[0].petgomoId",
            issues.single().location
        )
    }
}