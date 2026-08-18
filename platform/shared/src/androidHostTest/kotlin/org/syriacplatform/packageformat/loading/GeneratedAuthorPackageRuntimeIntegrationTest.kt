package org.syriacplatform.content.runtime

import java.nio.file.Files
import java.nio.file.Path
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import org.syriacplatform.common.result.Result
import org.syriacplatform.common.types.EntryPointId
import org.syriacplatform.common.types.OccasionId
import org.syriacplatform.packageformat.loading.ApplicationPackageLoader
import org.syriacplatform.packageformat.loading.PackageLoadResult
import org.syriacplatform.packageformat.loading.PackageSource
import org.syriacplatform.packagevalidation.compatibility.CoreCompatibilityDefaults

class GeneratedAuthorPackageRuntimeIntegrationTest {

    @Test
    fun generatedAuthorDatabasePreviewResolvesThroughRuntime() =
        runTest {
            val packageRoot =
                generatedPreviewPackageDirectory()

            assertTrue(
                Files.isDirectory(packageRoot),
                "Generated preview package was not found at: $packageRoot"
            )

            val loader =
                ApplicationPackageLoader(
                    source =
                        FileSystemTestPackageSource(
                            root = packageRoot
                        )
                )

            val loadResult =
                loader.load(
                    coreCompatibility =
                        CoreCompatibilityDefaults.CURRENT
                )

            val loaded =
                assertIs<
                        PackageLoadResult.Success
                        >(loadResult)

            assertTrue(
                loaded.validationReport.isValid
            )

            /*
             * From this point onward the test no longer
             * works with physical JSON.
             *
             * The accepted package enters the existing
             * runtime architecture exactly as a normal
             * application package does.
             */
            val store =
                RuntimeContentStore.from(
                    loaded.packageData
                )

            val resolver =
                RuntimeContentResolver(
                    store = store
                )

            val entryPointResult =
                resolver.resolveDefaultEntryPoint()

            val entryPointSuccess =
                assertIs<
                        Result.Success<RuntimeEntryPoint>
                        >(entryPointResult)

            val runtimeEntryPoint =
                entryPointSuccess.data

            assertEquals(
                EntryPointId(1L),
                runtimeEntryPoint.entryPoint.id
            )

            val runtimeOccasion =
                runtimeEntryPoint.occasion

            assertEquals(
                OccasionId(1L),
                runtimeOccasion.occasion.id
            )

            assertTrue(
                runtimeOccasion.prayerSequences.isNotEmpty()
            )

            /*
             * Every LiturgicalItem included in the
             * Development Preview must survive the entire
             * package -> validation -> runtime traversal.
             */
            val runtimeItems =
                runtimeOccasion
                    .prayerSequences
                    .flatMap {
                        it.items
                    }

            assertEquals(
                52,
                runtimeItems.size
            )

            /*
             * The preview currently consists only of
             * resolved Qolo occurrences.
             */
            val qoloTargets =
                runtimeItems.map { item ->
                    assertIs<
                            ResolvedLiturgicalItemTarget.Qolo
                            >(item.target)
                }

            assertEquals(
                52,
                qoloTargets.size
            )

            /*
             * effectiveMelodyId has already been resolved
             * by Build Tools. Runtime must resolve that
             * identifier to a Melody belonging to the same
             * canonical Qolo.
             */
            val resolvedTargets =
                qoloTargets.filter {
                    it.effectiveMelody != null
                }

            val ambiguousTargets =
                qoloTargets.filter {
                    it.effectiveMelody == null &&
                            it.melodyCandidates.size > 1
                }

            val unresolvedTargets =
                qoloTargets.filter {
                    it.effectiveMelody == null &&
                            it.melodyCandidates.isEmpty()
                }

            assertEquals(
                20,
                resolvedTargets.size
            )

            assertEquals(
                3,
                ambiguousTargets.size
            )

            assertEquals(
                29,
                unresolvedTargets.size
            )

            resolvedTargets.forEach { target ->
                assertEquals(
                    target.qolo.id,
                    target.effectiveMelody?.qoloId
                )
            }

            ambiguousTargets.forEach { target ->
                assertTrue(
                    target.melodyCandidates.size > 1
                )

                target.melodyCandidates.forEach { melody ->
                    assertEquals(
                        target.qolo.id,
                        melody.qoloId
                    )
                }
            }

            /*
             * Verify that real contextual verse data also
             * crossed the complete boundary.
             *
             * We do not hard-code one TextID here because
             * this is an integration test of the generated
             * representative source slice, not a synthetic
             * fixture.
             */
            val targetWithVerses =
                qoloTargets.firstOrNull {
                    it.verses.isNotEmpty()
                }

            assertNotNull(
                targetWithVerses,
                "No resolved Qolo occurrence contains contextual verses."
            )

            assertTrue(
                targetWithVerses.verses.all {
                    it.text.syriac.isNotBlank()
                }
            )

            /*
             * When a contextual Petgomo exists, Runtime
             * must expose the canonical Petgomo entity
             * rather than leaving an unresolved ID.
             */
            val resolvedPetgomos =
                qoloTargets
                    .flatMap {
                        it.verses
                    }
                    .mapNotNull {
                        it.petgomo
                    }

            resolvedPetgomos.forEach { petgomo ->
                assertTrue(
                    petgomo.syriac.isNotBlank()
                )
            }

            /*
             * PrayerSequence identity and authored
             * LiturgicalItem ordering are already encoded
             * in the package. Runtime must preserve them,
             * not reconstruct or resort them.
             */
            runtimeOccasion
                .prayerSequences
                .forEach { sequence ->

                    assertEquals(
                        sequence.sequence
                            .liturgicalItemIds,
                        sequence.items.map {
                            it.item.id
                        }
                    )
                }
        }

    private fun generatedPreviewPackageDirectory(): Path {
        return Path.of(
            "..",
            "..",
            "buildtools",
            "build",
            "generated",
            "occasion-1-preview"
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