package org.syriacplatform.packagevalidation.validators

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.syriacplatform.common.types.EntryPointId
import org.syriacplatform.common.types.ErrorCode
import org.syriacplatform.common.types.LiturgicalItemId
import org.syriacplatform.common.types.OccasionId
import org.syriacplatform.common.types.PetgomoId
import org.syriacplatform.common.types.PrayerId
import org.syriacplatform.common.types.PrayerSequenceId
import org.syriacplatform.common.types.TextId
import org.syriacplatform.content.models.EntryPoint
import org.syriacplatform.content.models.EntryPointTarget
import org.syriacplatform.content.models.LiturgicalItem
import org.syriacplatform.content.models.LiturgicalItemTarget
import org.syriacplatform.content.models.Occasion
import org.syriacplatform.content.models.Petgomo
import org.syriacplatform.content.models.Prayer
import org.syriacplatform.content.models.PrayerSequence
import org.syriacplatform.content.models.TextContent
import org.syriacplatform.packagevalidation.PackageValidationTestFixture.packageWith
import org.syriacplatform.packagevalidation.ValidationSeverity

class ReferenceValidatorTest {

    private val validator = ReferenceValidator()

    @Test
    fun validPackageProducesNoReferenceIssues() {
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
                    prayerSequenceIds = listOf(
                        PrayerSequenceId(301)
                    )
                )
            ),
            prayers = listOf(
                Prayer(
                    id = PrayerId(201),
                    name = "Evening Prayer",
                    description = null
                )
            ),
            prayerSequences = listOf(
                PrayerSequence(
                    id = PrayerSequenceId(301),
                    prayerId = PrayerId(201),
                    liturgicalItemIds = listOf(
                        LiturgicalItemId(501)
                    )
                )
            ),
            liturgicalItems = listOf(
                LiturgicalItem(
                    id = LiturgicalItemId(501),
                    target = LiturgicalItemTarget.Text(
                        textId = TextId(1001),
                        petgomoId = PetgomoId(15)
                    )
                )
            ),
            texts = listOf(
                TextContent(
                    id = TextId(1001),
                    syriac = "ܫܽܘܒܚܳܐ ܠܰܐܒܳܐ",
                    translations = emptyList()
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

        val issues = validator.validate(packageData)

        assertTrue(issues.isEmpty())
    }

    @Test
    fun validatorCollectsIssuesFromAllRegisteredReferenceRules() {
        val packageData = packageWith(
            entryPoints = listOf(
                EntryPoint(
                    id = EntryPointId(1),
                    name = "Broken Entry Point",
                    target = EntryPointTarget.Occasion(
                        occasionId = OccasionId(999)
                    ),
                    isDefault = true
                )
            ),
            occasions = listOf(
                Occasion(
                    id = OccasionId(101),
                    name = "Broken Occasion",
                    description = null,
                    prayerSequenceIds = listOf(
                        PrayerSequenceId(999)
                    )
                )
            ),
            prayerSequences = listOf(
                PrayerSequence(
                    id = PrayerSequenceId(301),
                    prayerId = PrayerId(999),
                    liturgicalItemIds = listOf(
                        LiturgicalItemId(999)
                    )
                )
            ),
            liturgicalItems = listOf(
                LiturgicalItem(
                    id = LiturgicalItemId(501),
                    target = LiturgicalItemTarget.Text(
                        textId = TextId(999),
                        petgomoId = PetgomoId(999)
                    )
                )
            )
        )

        val issues = validator.validate(packageData)

        assertEquals(
            6,
            issues.size
        )

        assertTrue(
            issues.all { issue ->
                issue.severity == ValidationSeverity.FATAL
            }
        )

        assertTrue(
            issues.all { issue ->
                issue.code == ErrorCode.INVALID_REFERENCE
            }
        )

        assertEquals(
            setOf(
                "entryPoints[1].target",
                "occasions[101].prayerSequenceIds[0]",
                "prayerSequences[301].prayerId",
                "prayerSequences[301].liturgicalItemIds[0]",
                "liturgicalItems[501].target.textId",
                "liturgicalItems[501].target.petgomoId"
            ),
            issues
                .mapNotNull { issue ->
                    issue.location
                }
                .toSet()
        )
    }
}