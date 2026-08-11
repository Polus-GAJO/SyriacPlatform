package org.syriacplatform.packageformat.loading

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import kotlin.test.assertEquals
import kotlin.test.assertIs
import org.syriacplatform.common.result.Result
import org.syriacplatform.common.types.ErrorCode
import org.syriacplatform.packageformat.models.PackageProfile
import org.syriacplatform.common.types.Version
import org.syriacplatform.packagevalidation.compatibility.CoreCompatibility

class ApplicationPackageLoaderTest {

    private class FakePackageSource(
        private val files: Map<String, ByteArray>
    ) : PackageSource {

        override suspend fun readBytesOrNull(
            path: String
        ): ByteArray? {
            return files[path]
        }
    }

    @Test
    fun discoverStructureDetectsPresentAndAbsentCollections() =
        runTest {
            val source =
                FakePackageSource(
                    files = mapOf(
                        PackagePaths.MANIFEST to
                                "{}".encodeToByteArray(),

                        PackagePaths.ENTRY_POINTS to
                                """{"items":[]}"""
                                    .encodeToByteArray(),

                        PackagePaths.PRAYERS to
                                """{"items":[]}"""
                                    .encodeToByteArray(),

                        PackagePaths.TEXTS to
                                """{"items":[]}"""
                                    .encodeToByteArray(),

                        PackagePaths.PETGOMOS to
                                """{"items":[]}"""
                                    .encodeToByteArray()
                    )
                )

            val loader =
                ApplicationPackageLoader(
                    source = source
                )

            val structure =
                loader.discoverStructure()

            assertTrue(
                structure.manifestPresent
            )

            assertTrue(
                structure.collectionPresence.entryPoints
            )

            assertTrue(
                structure.collectionPresence.prayers
            )

            assertTrue(
                structure.collectionPresence.texts
            )

            assertTrue(
                structure.collectionPresence.petgomos
            )

            assertFalse(
                structure.collectionPresence.occasions
            )

            assertFalse(
                structure.collectionPresence.qolos
            )

            assertFalse(
                structure.collectionPresence.melodies
            )

            assertFalse(
                structure.collectionPresence.qintos
            )

            assertFalse(
                structure.collectionPresence
                    .melodyQintoAssignments
            )
        }

    @Test
    fun discoverStructureDetectsMissingManifest() =
        runTest {
            val loader =
                ApplicationPackageLoader(
                    source =
                        FakePackageSource(
                            files = emptyMap()
                        )
                )

            val structure =
                loader.discoverStructure()

            assertFalse(
                structure.manifestPresent
            )
        }

    @Test
    fun loadManifestParsesAndMapsValidManifest() =
        runTest {
            val source =
                FakePackageSource(
                    files = mapOf(
                        PackagePaths.MANIFEST to
                                validManifestJson()
                                    .encodeToByteArray()
                    )
                )

            val loader =
                ApplicationPackageLoader(
                    source = source
                )

            val result =
                loader.loadManifest()

            val success =
                assertIs<Result.Success<*>>(result)

            val manifest =
                success.data as org.syriacplatform.packageformat.models.PackageManifest

            assertEquals(
                "test-package",
                manifest.packageId
            )

            assertEquals(
                PackageProfile.OCCASION,
                manifest.profile
            )
        }

    @Test
    fun missingManifestProducesStructureFailure() =
        runTest {
            val loader =
                ApplicationPackageLoader(
                    source =
                        FakePackageSource(
                            files = emptyMap()
                        )
                )

            val result =
                loader.loadManifest()

            val failure =
                assertIs<Result.Failure>(result)

            assertEquals(
                ErrorCode.PACKAGE_STRUCTURE_INVALID,
                failure.error.code
            )
        }

    @Test
    fun malformedManifestJsonProducesParseFailure() =
        runTest {
            val loader =
                ApplicationPackageLoader(
                    source =
                        FakePackageSource(
                            files = mapOf(
                                PackagePaths.MANIFEST to
                                        "{invalid-json"
                                            .encodeToByteArray()
                            )
                        )
                )

            val result =
                loader.loadManifest()

            val failure =
                assertIs<Result.Failure>(result)

            assertEquals(
                ErrorCode.PACKAGE_PARSE_FAILED,
                failure.error.code
            )
        }

    @Test
    fun loadBuildsAndValidatesCompleteOccasionPackage() =
        runTest {
            val emptyCollection =
                """{"items":[]}"""
                    .encodeToByteArray()

            val source =
                FakePackageSource(
                    files = mapOf(
                        PackagePaths.MANIFEST to
                                validManifestJson()
                                    .encodeToByteArray(),

                        PackagePaths.ENTRY_POINTS to
                                emptyCollection,

                        PackagePaths.OCCASIONS to
                                emptyCollection,

                        PackagePaths.PRAYERS to
                                emptyCollection,

                        PackagePaths.PRAYER_SEQUENCES to
                                emptyCollection,

                        PackagePaths.LITURGICAL_ITEMS to
                                emptyCollection,

                        PackagePaths.TEXTS to
                                emptyCollection
                    )
                )

            val loader =
                ApplicationPackageLoader(
                    source = source
                )

            val result =
                loader.load(
                    coreCompatibility =
                        CoreCompatibility(
                            version =
                                Version(1, 2, 0),
                            supportedSchemaVersions =
                                setOf(
                                    "1.0"
                                )
                        )
                )

            val success =
                assertIs<PackageLoadResult.Success>(
                    result
                )

            assertTrue(
                success.validationReport.isValid
            )

            assertTrue(
                success.packageData.entryPoints.isEmpty()
            )

            assertTrue(
                success.packageData.occasions.isEmpty()
            )

            assertTrue(
                success.packageData.prayers.isEmpty()
            )

            assertTrue(
                success.packageData.prayerSequences.isEmpty()
            )

            assertTrue(
                success.packageData.liturgicalItems.isEmpty()
            )

            assertTrue(
                success.packageData.texts.isEmpty()
            )

            assertTrue(
                success.packageData
                    .collectionPresence
                    .entryPoints
            )

            assertTrue(
                success.packageData
                    .collectionPresence
                    .occasions
            )

            assertFalse(
                success.packageData
                    .collectionPresence
                    .petgomos
            )

            assertFalse(
                success.packageData
                    .collectionPresence
                    .qintos
            )
        }

    @Test
    fun loadReturnsValidationFailedWhenRequiredOccasionCollectionIsMissing() =
        runTest {
            val emptyCollection =
                """{"items":[]}"""
                    .encodeToByteArray()

            val source =
                FakePackageSource(
                    files = mapOf(
                        PackagePaths.MANIFEST to
                                validManifestJson()
                                    .encodeToByteArray(),

                        PackagePaths.ENTRY_POINTS to
                                emptyCollection,

                        // OCCASIONS intentionally missing

                        PackagePaths.PRAYERS to
                                emptyCollection,

                        PackagePaths.PRAYER_SEQUENCES to
                                emptyCollection,

                        PackagePaths.LITURGICAL_ITEMS to
                                emptyCollection,

                        PackagePaths.TEXTS to
                                emptyCollection
                    )
                )

            val loader =
                ApplicationPackageLoader(
                    source = source
                )

            val result =
                loader.load(
                    coreCompatibility =
                        CoreCompatibility(
                            version = Version(1, 2, 0),
                            supportedSchemaVersions =
                                setOf("1.0")
                        )
                )

            val failure =
                assertIs<PackageLoadResult.ValidationFailed>(
                    result
                )

            assertFalse(
                failure.validationReport.isValid
            )

            assertTrue(
                failure.validationReport.issues.any { issue ->
                    issue.location ==
                            "collectionPresence.occasions"
                }
            )
        }

    @Test
    fun loadReturnsParseFailureWhenCollectionJsonIsMalformed() =
        runTest {
            val emptyCollection =
                """{"items":[]}"""
                    .encodeToByteArray()

            val source =
                FakePackageSource(
                    files = mapOf(
                        PackagePaths.MANIFEST to
                                validManifestJson()
                                    .encodeToByteArray(),

                        PackagePaths.ENTRY_POINTS to
                                "{invalid-json"
                                    .encodeToByteArray(),

                        PackagePaths.OCCASIONS to
                                emptyCollection,

                        PackagePaths.PRAYERS to
                                emptyCollection,

                        PackagePaths.PRAYER_SEQUENCES to
                                emptyCollection,

                        PackagePaths.LITURGICAL_ITEMS to
                                emptyCollection,

                        PackagePaths.TEXTS to
                                emptyCollection
                    )
                )

            val loader =
                ApplicationPackageLoader(
                    source = source
                )

            val result =
                loader.load(
                    coreCompatibility =
                        CoreCompatibility(
                            version = Version(1, 2, 0),
                            supportedSchemaVersions =
                                setOf("1.0")
                        )
                )

            val failure =
                assertIs<PackageLoadResult.Failure>(
                    result
                )

            assertEquals(
                ErrorCode.PACKAGE_PARSE_FAILED,
                failure.error.code
            )
        }

    private fun validManifestJson(): String {
        return """
            {
              "packageId": "test-package",
              "packageName": "Test Package",
              "schemaVersion": "1.0",
              "packageVersion": "1.0.0",
              "contentVersion": "1.0.0",
              "application": {
                "id": "test-app",
                "name": "Test App",
                "platform": "test",
                "defaultLanguage": "syr"
              },
              "profile": "occasion",
              "build": {
                "generatedAt": "2026-08-11T00:00:00Z",
                "buildTool": "test",
                "buildVersion": "1.0.0",
                "buildRevision": "test"
              },
              "compatibility": {
                "minimumCoreVersion": "1.0.0",
                "targetSchemaVersion": "1.0",
                "supportedFeatures": []
              }
            }
        """.trimIndent()
    }
}