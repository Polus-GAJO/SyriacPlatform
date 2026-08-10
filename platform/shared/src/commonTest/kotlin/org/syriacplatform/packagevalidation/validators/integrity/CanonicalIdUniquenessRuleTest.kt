package org.syriacplatform.packagevalidation.validators.integrity

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

class CanonicalIdUniquenessRuleTest {

    private val rule =
        CanonicalIdUniquenessRule()

    @Test
    fun duplicateCanonicalIdProducesFatalIssue() {
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
            "texts[id=25]",
            issues.single().location
        )
    }

    @Test
    fun repeatedUseOfSameCanonicalIdProducesNoIssues() {
        val packageData = packageWith(
            prayers = listOf(
                Prayer(
                    id = PrayerId(201),
                    name = "Test Prayer",
                    description = null
                )
            ),
            prayerSequences = listOf(
                PrayerSequence(
                    id = PrayerSequenceId(301),
                    prayerId = PrayerId(201),
                    liturgicalItemIds = listOf(
                        LiturgicalItemId(501),
                        LiturgicalItemId(502),
                        LiturgicalItemId(501)
                    )
                )
            ),
            liturgicalItems = listOf(
                LiturgicalItem(
                    id = LiturgicalItemId(501),
                    target = LiturgicalItemTarget.Text(
                        textId = TextId(25)
                    )
                ),
                LiturgicalItem(
                    id = LiturgicalItemId(502),
                    target = LiturgicalItemTarget.Text(
                        textId = TextId(25)
                    )
                )
            ),
            texts = listOf(
                TextContent(
                    id = TextId(25),
                    syriac = "Repeated liturgical text",
                    translations = emptyList()
                )
            )
        )

        val issues = rule.validate(packageData)

        assertTrue(issues.isEmpty())
    }

    @Test
    fun duplicateIdsInDifferentCollectionsDoNotConflict() {
        val packageData = packageWith(
            prayers = listOf(
                Prayer(
                    id = PrayerId(25),
                    name = "Prayer 25",
                    description = null
                )
            ),
            texts = listOf(
                TextContent(
                    id = TextId(25),
                    syriac = "Text 25",
                    translations = emptyList()
                )
            )
        )

        val issues = rule.validate(packageData)

        assertTrue(issues.isEmpty())
    }
}