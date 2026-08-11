package org.syriacplatform.packagevalidation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.syriacplatform.common.types.EntryPointId
import org.syriacplatform.common.types.GroupId
import org.syriacplatform.common.types.LiturgicalItemId
import org.syriacplatform.common.types.MelodyId
import org.syriacplatform.common.types.OccasionId
import org.syriacplatform.common.types.QoloId
import org.syriacplatform.common.types.TextId
import org.syriacplatform.content.models.EntryPoint
import org.syriacplatform.content.models.EntryPointTarget
import org.syriacplatform.content.models.LiturgicalItem
import org.syriacplatform.content.models.LiturgicalItemTarget
import org.syriacplatform.content.models.Melody
import org.syriacplatform.content.models.Qolo
import org.syriacplatform.content.models.TextContent
import org.syriacplatform.packagevalidation.PackageValidationTestFixture.packageWith
import org.syriacplatform.packagevalidation.PackageValidationTestFixture.validManifest

class PackageValidatorTest {

    private val validator = PackageValidator()

    @Test
    fun validPackageProducesValidReport() {
        val packageData = packageWith()

        val report = validator.validate(packageData)

        assertTrue(report.isValid)
        assertTrue(report.issues.isEmpty())
        assertTrue(report.fatalIssues.isEmpty())
    }

    @Test
    fun fatalIssueMakesReportInvalid() {
        val packageData = packageWith(
            manifest = validManifest().copy(
                packageId = ""
            )
        )

        val report = validator.validate(packageData)

        assertFalse(report.isValid)

        assertEquals(
            1,
            report.fatalIssues.size
        )

        assertEquals(
            "manifest.packageId",
            report.fatalIssues.single().location
        )
    }

    @Test
    fun validatorCollectsIssuesFromAllValidationLayers() {
        val packageData = packageWith(
            manifest = validManifest().copy(
                packageId = ""
            ),

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
            ),

            qolos = listOf(
                Qolo(
                    id = QoloId(438),
                    groupId = GroupId(12),
                    sort = 500,
                    name = "Qolo 438",
                    searchName = "Qolo 438",
                    poeticMeter = null
                ),
                Qolo(
                    id = QoloId(500),
                    groupId = GroupId(12),
                    sort = 600,
                    name = "Qolo 500",
                    searchName = "Qolo 500",
                    poeticMeter = null
                )
            ),

            melodies = listOf(
                Melody(
                    id = MelodyId(75),
                    qoloId = QoloId(500),
                    name = "Melody 75",
                    searchName = "Melody 75",
                    hasRecording = false
                )
            ),

            liturgicalItems = listOf(
                LiturgicalItem(
                    id = LiturgicalItemId(501),
                    target = LiturgicalItemTarget.Qolo(
                        qoloId = QoloId(438),
                        effectiveMelodyId = MelodyId(75)
                    )
                )
            )
        )

        val report = validator.validate(packageData)

        assertFalse(report.isValid)

        assertEquals(
            4,
            report.issues.size
        )

        assertEquals(
            4,
            report.fatalIssues.size
        )

        assertEquals(
            setOf(
                "manifest.packageId",
                "entryPoints[1].target",
                "texts[id=25]",
                "liturgicalItems[501].target.effectiveMelodyId"
            ),
            report.issues
                .mapNotNull { issue ->
                    issue.location
                }
                .toSet()
        )
    }
}