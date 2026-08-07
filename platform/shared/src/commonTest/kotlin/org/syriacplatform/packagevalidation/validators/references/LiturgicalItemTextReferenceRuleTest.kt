package org.syriacplatform.packagevalidation.validators.references

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.syriacplatform.common.types.LiturgicalItemId
import org.syriacplatform.common.types.TextId
import org.syriacplatform.content.models.LiturgicalItem
import org.syriacplatform.content.models.LiturgicalItemTarget
import org.syriacplatform.content.models.TextContent
import org.syriacplatform.packagevalidation.PackageValidationTestFixture.packageWith
import org.syriacplatform.packagevalidation.ValidationSeverity

class LiturgicalItemTextReferenceRuleTest {

    private val rule = LiturgicalItemTextReferenceRule()

    @Test
    fun existingTextReferenceProducesNoIssues() {
        val packageData = packageWith(
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
                    syriac = "ܫܽܘܒܚܳܐ ܠܰܐܒܳܐ",
                    translations = emptyList()
                )
            )
        )

        val issues = rule.validate(packageData)

        assertTrue(issues.isEmpty())
    }

    @Test
    fun missingTextReferenceProducesFatalIssue() {
        val packageData = packageWith(
            liturgicalItems = listOf(
                LiturgicalItem(
                    id = LiturgicalItemId(501),
                    target = LiturgicalItemTarget.Text(
                        textId = TextId(999)
                    )
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
            "liturgicalItems[501].target.textId",
            issues.single().location
        )
    }
}