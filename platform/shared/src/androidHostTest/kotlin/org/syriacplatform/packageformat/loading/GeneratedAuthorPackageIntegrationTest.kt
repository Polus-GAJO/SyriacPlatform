package org.syriacplatform.packageformat.loading

import java.nio.file.Files
import java.nio.file.Path
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue
import org.syriacplatform.common.result.Result
import org.syriacplatform.common.types.ErrorCode
import org.syriacplatform.content.repository.ApplicationPackageContentRepository
import org.syriacplatform.content.services.DefaultContentService
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
                "org.syriacplatform.preview.occasion$occasionId",
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

            assertTrue(
                packageData.liturgicalItems.isNotEmpty()
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
             * Preview media may be referenced by Melody recordings,
             * PerformanceMedia, or both.
             */
            assertTrue(
                packageData.collectionPresence.mediaAssets
            )

            assertTrue(
                packageData.mediaAssets.isNotEmpty()
            )

            val mediaAssetIds =
                packageData.mediaAssets
                    .map { it.id }
                    .toSet()

            val recordingIds =
                packageData.melodies
                    .flatMap { it.recordingIds }
                    .toSet()

            val performanceMediaAssetIds =
                packageData.performanceMedia
                  .map { it.mediaAssetId }
                  .toSet()

           assertEquals(
             mediaAssetIds,
             recordingIds + performanceMediaAssetIds
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

    @Test
    fun occasion378NoteLoadsThroughContentService() = runTest {
        if (occasionId != 378L) return@runTest

        val source = FileSystemTestPackageSource(
            root = generatedPreviewPackageDirectory()
        )
        val loader = ApplicationPackageLoader(source = source)
        val packageResult = assertIs<PackageLoadResult.Success>(
            loader.load(CoreCompatibilityDefaults.CURRENT)
        )
        val expectedNote = packageResult.packageData.notes.single { it.id == 347L }
        assertTrue(!expectedNote.text.isNullOrBlank())

        val repository = ApplicationPackageContentRepository(
            loader = loader,
            coreCompatibility = CoreCompatibilityDefaults.CURRENT
        )
        val service = DefaultContentService(repository)
        val loaded = assertIs<Result.Success<org.syriacplatform.content.models.Note>>(
            service.loadNote(347L)
        )
        assertEquals(expectedNote, loaded.data)

        val missing = assertIs<Result.Failure>(
            service.loadNote(Long.MAX_VALUE)
        )
        assertEquals(ErrorCode.CONTENT_NOT_FOUND, missing.error.code)
    }

    private val occasionId: Long
        get() =
            requireNotNull(
                System.getProperty(
                    "syriacplatform.test.occasionId"
                )
            ).toLong()

    private fun generatedPreviewPackageDirectory(): Path {
        return Path.of(
            "..",
            "..",
            "buildtools",
            "build",
            "generated",
            "occasion-$occasionId-preview"
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