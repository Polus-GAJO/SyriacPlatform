package org.syriacplatform.packagevalidation.validators.references

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.syriacplatform.common.types.LiturgicalItemId
import org.syriacplatform.common.types.PrayerId
import org.syriacplatform.common.types.PrayerSequenceId
import org.syriacplatform.common.types.TextId
import org.syriacplatform.content.models.LiturgicalItem
import org.syriacplatform.content.models.LiturgicalItemTarget
import org.syriacplatform.content.models.Prayer
import org.syriacplatform.content.models.PrayerSequence
import org.syriacplatform.content.models.TextContent
import org.syriacplatform.packagevalidation.PackageValidationTestFixture.packageWith
import org.syriacplatform.packagevalidation.ValidationSeverity

class PrayerSequenceLiturgicalItemReferenceRuleTest {

    private val rule =
        PrayerSequenceLiturgicalItemReferenceRule()

    @Test
    fun existingPrayerSequenceLiturgicalItemReferencesProduceNoIssues() {
        val packageData = packageWith(
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
                        LiturgicalItemId(501),
                        LiturgicalItemId(502)
                    )
                )
            ),
            liturgicalItems = listOf(
                LiturgicalItem(
                    id = LiturgicalItemId(501),
                    target = LiturgicalItemTarget.Text(
                        textId = TextId(1001)
                    )
                ),
                LiturgicalItem(
                    id = LiturgicalItemId(502),
                    target = LiturgicalItemTarget.Text(
                        textId = TextId(1002)
                    )
                )
            ),
            texts = listOf(
                TextContent(
                    id = TextId(1001),
                    syriac = "Test text 1",
                    translations = emptyList()
                ),
                TextContent(
                    id = TextId(1002),
                    syriac = "Test text 2",
                    translations = emptyList()
                )
            )
        )

        val issues = rule.validate(packageData)

        assertTrue(issues.isEmpty())
    }

    @Test
    fun missingPrayerSequenceLiturgicalItemReferenceProducesFatalIssue() {
        val packageData = packageWith(
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
                        LiturgicalItemId(501),
                        LiturgicalItemId(999)
                    )
                )
            ),
            liturgicalItems = listOf(
                LiturgicalItem(
                    id = LiturgicalItemId(501),
                    target = LiturgicalItemTarget.Text(
                        textId = TextId(1001)
                    )
                )
            ),
            texts = listOf(
                TextContent(
                    id = TextId(1001),
                    syriac = "Test text 1",
                    translations = emptyList()
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
            "prayerSequences[301].liturgicalItemIds[1]",
            issues.single().location
        )
    }
}