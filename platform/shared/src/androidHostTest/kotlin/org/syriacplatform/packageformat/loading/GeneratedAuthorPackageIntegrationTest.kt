package org.syriacplatform.packageformat.loading

import java.nio.file.Files
import java.nio.file.Path
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue
import org.syriacplatform.packagevalidation.compatibility.CoreCompatibilityDefaults

class GeneratedAuthorPackageIntegrationTest {

    @Test
    fun generatedAuthorDatabasePreviewLoadsThroughExistingCore() =
        runTest {
            val packageRoot =
                generatedPreviewPackageDirectory()

            println(
                "TEST user.dir = " +
                        System.getProperty("user.dir")
            )

            println(
                "PACKAGE ROOT = " +
                        packageRoot
            )

            println(
                "PACKAGE EXISTS = " +
                        Files.isDirectory(packageRoot)
            )

            assertTrue(
                Files.isDirectory(packageRoot),
                "Generated preview package was not found at: $packageRoot"
            )

            val source =
                FileSystemTestPackageSource(
                    root = packageRoot
                )

            val loader =
                ApplicationPackageLoader(
                    source = source
                )

            val result =
                loader.load(
                    coreCompatibility =
                        CoreCompatibilityDefaults.CURRENT
                )

            if (result is PackageLoadResult.ValidationFailed) {
                println(
                    "VALIDATION FAILED WITH " +
                            "${result.validationReport.issues.size} ISSUE(S)"
                )

                result.validationReport.issues.forEach { issue ->
                    println(
                        "${issue.severity} | " +
                                "${issue.code} | " +
                                "${issue.location ?: "<no location>"} | " +
                                issue.message
                    )
                }
            }

            val success =
                assertIs<PackageLoadResult.Success>(
                    result
                )

            assertTrue(
                success.validationReport.isValid
            )

            val packageData =
                success.packageData

            assertEquals(
                "org.syriacplatform.preview.occasion2",
                packageData.manifest.packageId
            )

            assertEquals(
                1,
                packageData.entryPoints.size
            )

            assertEquals(
                1,
                packageData.occasions.size
            )

            assertEquals(
                51,
                packageData.liturgicalItems.size
            )

            assertTrue(
                packageData.prayers.isNotEmpty()
            )

            assertTrue(
                packageData.prayerSequences.isNotEmpty()
            )

            assertTrue(
                packageData.texts.isNotEmpty()
            )

            assertTrue(
                packageData.qolos.isNotEmpty()
            )

            assertTrue(
                packageData.melodies.isNotEmpty()
            )

            /*
             * Occasion 2 is the verified media-aware preview:
             * Build Tools selected and packaged exactly the
             * MediaAssets referenced by its Melody recordingIds.
             */
            assertTrue(
                packageData.collectionPresence.mediaAssets
            )

            assertEquals(
                13,
                packageData.mediaAssets.size
            )

            val mediaAssetIds =
                packageData.mediaAssets
                    .map { it.id }
                    .toSet()

            val recordingIds =
                packageData.melodies
                    .flatMap { it.recordingIds }
                    .toSet()

            assertTrue(
                recordingIds.isNotEmpty()
            )

            assertEquals(
                mediaAssetIds,
                recordingIds
            )

            /*
             * The development preview deliberately excludes
             * unresolved Qinto authoring data.
             */
            assertTrue(
                packageData.qintos.isEmpty()
            )

            assertTrue(
                packageData.melodyQintoAssignments.isEmpty()
            )

            /*
             * The Core must preserve physical collection
             * presence independently from collection size.
             */
            assertTrue(
                packageData.collectionPresence.entryPoints
            )

            assertTrue(
                packageData.collectionPresence.occasions
            )

            assertTrue(
                packageData.collectionPresence.prayers
            )

            assertTrue(
                packageData.collectionPresence.prayerSequences
            )

            assertTrue(
                packageData.collectionPresence.liturgicalItems
            )

            assertTrue(
                packageData.collectionPresence.texts
            )

            assertTrue(
                packageData.collectionPresence.qolos
            )

            assertTrue(
                packageData.collectionPresence.melodies
            )

            assertFalse(
                packageData.collectionPresence.qintos
            )

            assertFalse(
                packageData.collectionPresence
                    .melodyQintoAssignments
            )
        }

    private fun generatedPreviewPackageDirectory(): Path {
        return Path.of(
            "..",
            "..",
            "buildtools",
            "build",
            "generated",
            "occasion-2-preview"
        ).toAbsolutePath().normalize()
    }

    private class FileSystemTestPackageSource(
        private val root: Path
    ) : PackageSource {

        override suspend fun readBytesOrNull(
            path: String
        ): ByteArray? {
            val file =
                root.resolve(path)
                    .normalize()

            require(
                file.startsWith(root)
            ) {
                "Package path escapes package root: $path"
            }

            return if (
                Files.isRegularFile(file)
            ) {
                Files.readAllBytes(file)
            } else {
                null
            }
        }
    }
}