package org.syriacplatform.packagevalidation.validators.integrity

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.syriacplatform.common.types.LiturgicalItemId
import org.syriacplatform.common.types.QoloId
import org.syriacplatform.common.types.TextId
import org.syriacplatform.common.types.TextOccurrenceId
import org.syriacplatform.content.models.LiturgicalItem
import org.syriacplatform.content.models.LiturgicalItemTarget
import org.syriacplatform.content.models.TextOccurrence
import org.syriacplatform.packagevalidation.PackageValidationTestFixture.packageWith
import org.syriacplatform.packagevalidation.ValidationSeverity

class TextOccurrenceIdUniquenessRuleTest {

    private val rule =
        TextOccurrenceIdUniquenessRule()

    @Test
    fun duplicateTextOccurrenceIdProducesFatalIssue() {
        val packageData =
            packageWith(
                liturgicalItems = listOf(
                    LiturgicalItem(
                        id = LiturgicalItemId(501),
                        target =
                            LiturgicalItemTarget.Qolo(
                                qoloId = QoloId(801),
                                verses = listOf(
                                    TextOccurrence(
                                        id = TextOccurrenceId(9001),
                                        textId = TextId(601)
                                    )
                                )
                            )
                    ),
                    LiturgicalItem(
                        id = LiturgicalItemId(502),
                        target =
                            LiturgicalItemTarget.UnresolvedQolo(
                                verses = listOf(
                                    TextOccurrence(
                                        id = TextOccurrenceId(9001),
                                        textId = TextId(602)
                                    )
                                )
                            )
                    )
                )
            )

        val issues =
            rule.validate(packageData)

        assertEquals(1, issues.size)
        assertEquals(
            ValidationSeverity.FATAL,
            issues.single().severity
        )
        assertEquals(
            "liturgicalItems.textOccurrences[id=9001]",
            issues.single().location
        )
    }

    @Test
    fun repeatedTextIdWithDistinctOccurrenceIdsProducesNoIssues() {
        val packageData =
            packageWith(
                liturgicalItems = listOf(
                    LiturgicalItem(
                        id = LiturgicalItemId(501),
                        target =
                            LiturgicalItemTarget.Qolo(
                                qoloId = QoloId(801),
                                verses = listOf(
                                    TextOccurrence(
                                        id = TextOccurrenceId(9001),
                                        textId = TextId(601)
                                    ),
                                    TextOccurrence(
                                        id = TextOccurrenceId(9002),
                                        textId = TextId(601)
                                    )
                                )
                            )
                    )
                )
            )

        assertTrue(
            rule.validate(packageData).isEmpty()
        )
    }
}
