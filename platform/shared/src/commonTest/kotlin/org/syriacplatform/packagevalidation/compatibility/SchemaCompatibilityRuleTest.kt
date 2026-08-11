package org.syriacplatform.packagevalidation.compatibility

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.syriacplatform.common.types.Version
import org.syriacplatform.packagevalidation.PackageValidationTestFixture.validManifest
import org.syriacplatform.packagevalidation.ValidationSeverity

class SchemaCompatibilityRuleTest {

    private val coreCompatibility =
        CoreCompatibility(
            version = Version(1, 0, 0),
            supportedSchemaVersions = setOf(
                "1.0"
            )
        )

    private val rule =
        SchemaCompatibilityRule(
            coreCompatibility = coreCompatibility
        )

    @Test
    fun supportedMatchingSchemaProducesNoIssues() {
        val manifest =
            validManifest().copy(
                schemaVersion = "1.0",
                compatibility =
                    validManifest().compatibility.copy(
                        targetSchemaVersion = "1.0"
                    )
            )

        val issues = rule.validate(manifest)

        assertTrue(issues.isEmpty())
    }

    @Test
    fun unsupportedSchemaProducesFatalIssue() {
        val manifest =
            validManifest().copy(
                schemaVersion = "2.0",
                compatibility =
                    validManifest().compatibility.copy(
                        targetSchemaVersion = "2.0"
                    )
            )

        val issues = rule.validate(manifest)

        assertEquals(
            1,
            issues.size
        )

        assertEquals(
            ValidationSeverity.FATAL,
            issues.single().severity
        )

        assertEquals(
            "manifest.schemaVersion",
            issues.single().location
        )
    }

    @Test
    fun targetSchemaDifferentFromPackageSchemaProducesFatalIssue() {
        val manifest =
            validManifest().copy(
                schemaVersion = "1.0",
                compatibility =
                    validManifest().compatibility.copy(
                        targetSchemaVersion = "2.0"
                    )
            )

        val issues = rule.validate(manifest)

        assertEquals(
            1,
            issues.size
        )

        assertEquals(
            ValidationSeverity.FATAL,
            issues.single().severity
        )

        assertEquals(
            "manifest.compatibility.targetSchemaVersion",
            issues.single().location
        )
    }
}