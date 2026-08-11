package org.syriacplatform.packagevalidation.compatibility

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.syriacplatform.common.types.Version
import org.syriacplatform.packagevalidation.PackageValidationTestFixture.validManifest
import org.syriacplatform.packagevalidation.ValidationSeverity

class MinimumCoreVersionRuleTest {

    private val coreCompatibility =
        CoreCompatibility(
            version = Version(1, 2, 0),
            supportedSchemaVersions = setOf(
                "1.0"
            )
        )

    private val rule =
        MinimumCoreVersionRule(
            coreCompatibility = coreCompatibility
        )

    @Test
    fun olderMinimumCoreVersionProducesNoIssues() {
        val manifest =
            validManifest().copy(
                compatibility =
                    validManifest().compatibility.copy(
                        minimumCoreVersion = "1.0.0"
                    )
            )

        val issues = rule.validate(manifest)

        assertTrue(issues.isEmpty())
    }

    @Test
    fun equalMinimumCoreVersionProducesNoIssues() {
        val manifest =
            validManifest().copy(
                compatibility =
                    validManifest().compatibility.copy(
                        minimumCoreVersion = "1.2.0"
                    )
            )

        val issues = rule.validate(manifest)

        assertTrue(issues.isEmpty())
    }

    @Test
    fun newerMinimumCoreVersionProducesFatalIssue() {
        val manifest =
            validManifest().copy(
                compatibility =
                    validManifest().compatibility.copy(
                        minimumCoreVersion = "1.3.0"
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
            "manifest.compatibility.minimumCoreVersion",
            issues.single().location
        )
    }

    @Test
    fun invalidMinimumCoreVersionProducesFatalIssue() {
        val manifest =
            validManifest().copy(
                compatibility =
                    validManifest().compatibility.copy(
                        minimumCoreVersion = "invalid-version"
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
            "manifest.compatibility.minimumCoreVersion",
            issues.single().location
        )
    }
}