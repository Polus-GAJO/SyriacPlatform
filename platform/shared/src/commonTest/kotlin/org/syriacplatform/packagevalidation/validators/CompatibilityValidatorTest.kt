package org.syriacplatform.packagevalidation.validators

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.syriacplatform.common.types.Version
import org.syriacplatform.packagevalidation.PackageValidationTestFixture.validManifest
import org.syriacplatform.packagevalidation.ValidationSeverity
import org.syriacplatform.packagevalidation.compatibility.CoreCompatibility

class CompatibilityValidatorTest {

    private val coreCompatibility =
        CoreCompatibility(
            version = Version(1, 2, 0),
            supportedSchemaVersions = setOf(
                "1.0"
            )
        )

    private val validator =
        CompatibilityValidator(
            coreCompatibility = coreCompatibility
        )

    @Test
    fun compatibleManifestProducesNoIssues() {
        val manifest =
            validManifest().copy(
                schemaVersion = "1.0",
                compatibility =
                    validManifest().compatibility.copy(
                        targetSchemaVersion = "1.0",
                        minimumCoreVersion = "1.0.0"
                    )
            )

        val issues = validator.validate(manifest)

        assertTrue(issues.isEmpty())
    }

    @Test
    fun validatorCollectsIssuesFromBothCompatibilityRules() {
        val manifest =
            validManifest().copy(
                schemaVersion = "2.0",
                compatibility =
                    validManifest().compatibility.copy(
                        targetSchemaVersion = "2.0",
                        minimumCoreVersion = "2.0.0"
                    )
            )

        val issues = validator.validate(manifest)

        assertEquals(
            2,
            issues.size
        )

        assertTrue(
            issues.all { issue ->
                issue.severity == ValidationSeverity.FATAL
            }
        )

        assertEquals(
            setOf(
                "manifest.schemaVersion",
                "manifest.compatibility.minimumCoreVersion"
            ),
            issues
                .mapNotNull { issue ->
                    issue.location
                }
                .toSet()
        )
    }

    @Test
    fun targetSchemaMismatchIsCollectedByValidator() {
        val manifest =
            validManifest().copy(
                schemaVersion = "1.0",
                compatibility =
                    validManifest().compatibility.copy(
                        targetSchemaVersion = "2.0",
                        minimumCoreVersion = "1.0.0"
                    )
            )

        val issues = validator.validate(manifest)

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