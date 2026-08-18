package org.syriacplatform.buildtools.schema

import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import org.syriacplatform.buildtools.source.AuthorSourceDataLoader

class MelodyResolverTest {

    private val loader = AuthorSourceDataLoader()
    private val resolver = MelodyResolver()

    @Test
    fun representativeSliceProducesExpectedResolutionClasses() {
        val source = loadSource()

        val resolutions = source.existsIn.map { item ->
            item to resolver.resolve(
                item = item,
                melodies = source.melodies
            )
        }

        assertEquals(
            20,
            resolutions.count {
                it.second is MelodyResolution.Resolved
            }
        )

        assertEquals(
            29,
            resolutions.count {
                it.second is MelodyResolution.UnresolvedQinto
            }
        )

        assertEquals(
            3,
            resolutions.count {
                it.second is MelodyResolution.Ambiguous
            }
        )

        assertEquals(
            0,
            resolutions.count {
                it.second is MelodyResolution.NoCandidate
            }
        )

        assertEquals(
            52,
            resolutions.size
        )
    }

    @Test
    fun distinguishesNullAndZeroUndeterminedQinto() {
        val source = loadSource()

        val unresolved = source.existsIn
            .map { item ->
                resolver.resolve(
                    item,
                    source.melodies
                )
            }
            .filterIsInstance<
                    MelodyResolution.UnresolvedQinto
                    >()

        assertEquals(
            16,
            unresolved.count {
                it.qintoId == null
            }
        )

        assertEquals(
            13,
            unresolved.count {
                it.qintoId == 0L
            }
        )
    }

    @Test
    fun resolvesSingleMelodyCandidate() {
        val source = loadSource()

        val result = source.existsIn
            .asSequence()
            .map { item ->
                item to resolver.resolve(
                    item,
                    source.melodies
                )
            }
            .first {
                it.second is MelodyResolution.Resolved
            }

        val item = result.first

        val resolution = assertIs<
                MelodyResolution.Resolved
                >(result.second)

        val melody = source.melodies.single {
            it.id == resolution.melodyId
        }

        assertEquals(
            item.qoloId,
            melody.qoloId
        )

        assertEquals(
            item.qintoId,
            melody.qintoId
        )
    }

    @Test
    fun preservesKnownAmbiguousMelodyCase() {
        val source = loadSource()

        val ambiguousItems = source.existsIn
            .mapNotNull { item ->
                when (
                    val resolution = resolver.resolve(
                        item,
                        source.melodies
                    )
                ) {
                    is MelodyResolution.Ambiguous ->
                        item to resolution

                    else -> null
                }
            }

        assertEquals(
            3,
            ambiguousItems.size
        )

        ambiguousItems.forEach {
                (item, resolution) ->

            assertEquals(
                116L,
                item.qoloId
            )

            assertEquals(
                1L,
                item.qintoId
            )

            assertEquals(
                listOf(
                    119L,
                    1965L
                ),
                resolution.melodyIds
            )
        }
    }

    @Test
    fun resolverNeverChoosesFromAmbiguousCandidates() {
        val source = loadSource()

        val ambiguousItem = source.existsIn
            .first { item ->
                item.qoloId == 116L &&
                        item.qintoId == 1L
            }

        val resolution = resolver.resolve(
            ambiguousItem,
            source.melodies
        )

        assertTrue(
            resolution is MelodyResolution.Ambiguous
        )
    }

    private fun loadSource() =
        loader.load(
            representativeExportDirectory()
        )

    private fun representativeExportDirectory(): Path {
        return Path.of(
            "..",
            "author-database",
            "samples",
            "mapping-analysis"
        ).toAbsolutePath().normalize()
    }
}