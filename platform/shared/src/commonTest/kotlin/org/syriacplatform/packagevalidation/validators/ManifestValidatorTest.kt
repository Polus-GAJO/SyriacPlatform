package org.syriacplatform.packagevalidation.validators

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.syriacplatform.common.types.ErrorCode
import org.syriacplatform.packagevalidation.ValidationSeverity
import org.syriacplatform.packagevalidation.PackageValidationTestFixture.validManifest

class ManifestValidatorTest {

    private val validator = ManifestValidator()

    @Test
    fun completeManifestProducesNoIssues() {
        val issues = validator.validate(
            validManifest()
        )

        assertTrue(issues.isEmpty())
    }

    @Test
    fun blankRequiredValuesProduceAllIssues() {
        val manifest = validManifest().copy(
            packageId = "",
            schemaVersion = " ",
            application = validManifest().application.copy(
                id = ""
            ),
            build = validManifest().build.copy(
                buildRevision = ""
            ),
            compatibility =
                validManifest().compatibility.copy(
                    minimumCoreVersion = ""
                )
        )

        val issues = validator.validate(manifest)

        assertEquals(5, issues.size)

        assertTrue(
            issues.all { issue ->
                issue.severity == ValidationSeverity.FATAL
            }
        )

        assertTrue(
            issues.all { issue ->
                issue.code == ErrorCode.MISSING_REQUIRED_FIELD
            }
        )

        assertEquals(
            listOf(
                "manifest.packageId",
                "manifest.schemaVersion",
                "manifest.application.id",
                "manifest.build.buildRevision",
                "manifest.compatibility.minimumCoreVersion"
            ),
            issues.map { issue ->
                issue.location
            }
        )
    }

    @Test
    fun blankAndDuplicateSupportedFeaturesProduceIssues() {
        val manifest = validManifest().copy(
            compatibility =
                validManifest().compatibility.copy(
                    supportedFeatures = listOf(
                        "canonical-content",
                        " ",
                        "search-index",
                        "canonical-content"
                    )
                )
        )

        val issues = validator.validate(manifest)

        assertEquals(2, issues.size)

        assertEquals(
            listOf(
                "manifest.compatibility.supportedFeatures[1]",
                "manifest.compatibility.supportedFeatures"
            ),
            issues.map { issue ->
                issue.location
            }
        )
    }
}

