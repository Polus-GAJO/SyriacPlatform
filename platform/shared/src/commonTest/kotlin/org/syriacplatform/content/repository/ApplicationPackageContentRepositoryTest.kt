package org.syriacplatform.content.repository

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.coroutines.test.runTest
import org.syriacplatform.common.result.Result
import org.syriacplatform.common.types.QoloId
import org.syriacplatform.common.types.Version
import org.syriacplatform.packageformat.loading.ApplicationPackageLoader
import org.syriacplatform.packageformat.loading.PackagePaths
import org.syriacplatform.packageformat.loading.PackageSource
import org.syriacplatform.packagevalidation.compatibility.CoreCompatibility

class ApplicationPackageContentRepositoryTest {

    private class FakePackageSource(
        private val files: Map<String, ByteArray>
    ) : PackageSource {

        override suspend fun readBytesOrNull(
            path: String
        ): ByteArray? {
            return files[path]
        }
    }

    private val coreCompatibility =
        CoreCompatibility(
            version = Version(1, 2, 0),
            supportedSchemaVersions = setOf(
                "1.0"
            )
        )

    @Test
    fun loadAllQolosReturnsQolosFromValidatedPackage() =
        runTest {
            val repository =
                createRepository()

            val result =
                repository.loadAllQolos()

            val success =
                assertIs<Result.Success<List<*>>>(
                    result
                )

            assertEquals(
                2,
                success.data.size
            )
        }

    @Test
    fun loadQoloReturnsRequestedQoloFromValidatedPackage() =
        runTest {
            val repository =
                createRepository()

            val result =
                repository.loadQolo(
                    QoloId(438)
                )

            val success =
                assertIs<Result.Success<*>>(
                    result
                )

            val qolo =
                success.data as
                        org.syriacplatform.content.models.Qolo

            assertEquals(
                QoloId(438),
                qolo.id
            )

            assertEquals(
                "Qolo 438",
                qolo.name
            )
        }

    @Test
    fun loadQoloReturnsContentNotFoundForUnknownId() =
        runTest {
            val repository =
                createRepository()

            val result =
                repository.loadQolo(
                    QoloId(999)
                )

            val failure =
                assertIs<Result.Failure>(
                    result
                )

            assertEquals(
                org.syriacplatform.common.types.ErrorCode.CONTENT_NOT_FOUND,
                failure.error.code
            )
        }

    private fun createRepository():
            ApplicationPackageContentRepository {

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
                            emptyCollection,

                    PackagePaths.QOLOS to
                            qolosJson()
                                .encodeToByteArray()
                )
            )

        return ApplicationPackageContentRepository(
            loader =
                ApplicationPackageLoader(
                    source = source
                ),
            coreCompatibility =
                coreCompatibility
        )
    }

    private fun qolosJson(): String {
        return """
            {
              "items": [
                {
                  "id": 438,
                  "groupId": 12,
                  "sort": 500,
                  "name": "Qolo 438",
                  "searchName": "Qolo 438",
                  "poeticMeter": null
                },
                {
                  "id": 439,
                  "groupId": 12,
                  "sort": 510,
                  "name": "Qolo 439",
                  "searchName": "Qolo 439",
                  "poeticMeter": null
                }
              ]
            }
        """.trimIndent()
    }

    private fun validManifestJson(): String {
        return """
            {
              "packageId": "repository-test",
              "packageName": "Repository Test",
              "schemaVersion": "1.0",
              "packageVersion": "1.0.0",
              "contentVersion": "1.0.0",
              "application": {
                "id": "repository-test-app",
                "name": "Repository Test",
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