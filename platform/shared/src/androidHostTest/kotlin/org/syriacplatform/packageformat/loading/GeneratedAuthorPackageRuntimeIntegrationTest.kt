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
                when (loadResult) {
                    is PackageLoadResult.Success ->
                        loadResult

                    is PackageLoadResult.ValidationFailed -> {
                        val fatalDetails =
                            loadResult.validationReport
                                .fatalIssues
                                .joinToString(
                                    separator = "\n"
                                ) { issue ->
                                    buildString {
                                        append(issue.code)
                                        append(": ")
                                        append(issue.message)

                                        issue.location
                                            ?.let { location ->
                                                append(" [")
                                                append(location)
                                                append("]")
                                            }
                                    }
                                }

                        error(
                            "Generated preview package failed validation " +
                                "with " +
                                loadResult.validationReport
                                    .fatalIssues
                                    .size +
                                " fatal issue(s):\n" +
                                fatalDetails
                        )
                    }

                    is PackageLoadResult.Failure ->
                        error(
                            "Generated preview package failed to load: " +
                                loadResult.error
                        )
                }

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

            assertTrue(
                store.content.mediaAssets.isNotEmpty()
            )

            assertEquals(
                store.content.mediaAssets
                    .map { it.id }
                    .toSet(),
                store.index.mediaAssetsById.keys
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
                EntryPointId(occasionId),
                runtimeEntryPoint.entryPoint.id
            )

            val runtimeOccasion =
                runtimeEntryPoint.occasion

            assertEquals(
                OccasionId(occasionId),
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

            assertTrue(
                runtimeItems.isNotEmpty()
            )

            /*
             * Real author exports may contain both Qolo occurrences
             * and non-melodic text groups. Preserve and verify both.
             */
            val qoloTargets = runtimeItems.mapNotNull {
                it.target as? ResolvedLiturgicalItemTarget.Qolo
            }
            val nonMelodicTargets = runtimeItems.mapNotNull {
                it.target as? ResolvedLiturgicalItemTarget.NonMelodicText
            }
            val unresolvedQoloTargets = runtimeItems.mapNotNull {
                it.target as? ResolvedLiturgicalItemTarget.UnresolvedQolo
            }

            assertEquals(
                runtimeItems.size,
                qoloTargets.size + nonMelodicTargets.size +
                    unresolvedQoloTargets.size
            )

            nonMelodicTargets.forEach { target ->
                assertTrue(target.verses.isNotEmpty())
                assertTrue(target.verses.all { it.text.syriac.isNotBlank() })
            }

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

            /*
             * The exact resolved / ambiguous / unresolved split
             * is author-data dependent. What Runtime must preserve
             * is that every Qolo target belongs to exactly one of
             * these canonical resolution states.
             */
            assertEquals(
                qoloTargets.size,
                resolvedTargets.size +
                        ambiguousTargets.size +
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

    /**
     * Occasion 379 is the real author-export translation smoke test:
     * TextIDs 6064-6066 have published ar/en translations; 6067 does not.
     * Exercise the complete JSON -> package loader -> runtime index ->
     * resolved liturgical verse path, without changing UI behavior.
     */
    @Test
    fun publishedTranslationsFromGeneratedPreviewReachRuntimeVerses() = runTest {
        if (occasionId != 379L) return@runTest

        val loader = ApplicationPackageLoader(
            source = FileSystemTestPackageSource(generatedPreviewPackageDirectory())
        )
        val loaded = assertIs<PackageLoadResult.Success>(
            loader.load(coreCompatibility = CoreCompatibilityDefaults.CURRENT)
        )
        val store = RuntimeContentStore.from(loaded.packageData)
        val resolver = RuntimeContentResolver(store)
        val occasion = assertIs<Result.Success<RuntimeEntryPoint>>(
            resolver.resolveDefaultEntryPoint()
        ).data.occasion

        val verses = occasion.prayerSequences.flatMap { sequence ->
            sequence.items.flatMap { resolved ->
                when (val target = resolved.target) {
                    is ResolvedLiturgicalItemTarget.Qolo -> target.verses
                    is ResolvedLiturgicalItemTarget.NonMelodicText -> target.verses
                    is ResolvedLiturgicalItemTarget.UnresolvedQolo -> target.verses
                    is ResolvedLiturgicalItemTarget.Text -> emptyList()
                }
            }
        }
        val canonicalTexts = store.content.texts.associateBy { it.id.value }
        listOf(6064L, 6065L, 6066L).forEach { textId ->
            val text = assertNotNull(canonicalTexts[textId], "Missing canonical TextID=$textId")
            assertEquals(
                setOf("ar", "en"),
                text.translations.map { it.language }.toSet(),
                "Wrong published languages for TextID=$textId"
            )
            assertEquals(2, text.translations.size)
            assertTrue(text.translations.all { it.content.isNotBlank() })
            val occurrences = verses.filter { it.text.id.value == textId }
            assertTrue(occurrences.isNotEmpty(), "TextID=$textId did not reach runtime verses")
            occurrences.forEach { occurrence ->
                assertEquals(text.translations, occurrence.text.translations)
            }
        }
        assertTrue(
            assertNotNull(canonicalTexts[6067L]).translations.isEmpty(),
            "Unpublished TextID=6067 must not contain translations"
        )
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