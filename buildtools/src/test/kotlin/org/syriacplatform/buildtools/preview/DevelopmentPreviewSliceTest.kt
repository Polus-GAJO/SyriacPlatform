package org.syriacplatform.buildtools.preview

import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.syriacplatform.buildtools.schema.SchemaV1CompositionMapper
import org.syriacplatform.buildtools.schema.SchemaV1NavigationMapper
import org.syriacplatform.buildtools.source.AuthorSourceDataLoader

class DevelopmentPreviewSliceTest {

    private val loader =
        AuthorSourceDataLoader()

    private val compositionMapper =
        SchemaV1CompositionMapper()

    private val previewSlice =
        DevelopmentPreviewSlice()

    private val navigationMapper =
        SchemaV1NavigationMapper()

    @Test
    fun previewContainsAllLegalOccurrences() {
        val source = loadSource()

        val full =
            compositionMapper.map(source)

        val preview =
            previewSlice.create(full)

        val items =
            preview.prayers.flatMap {
                it.resolvedItems
            }

        assertEquals(
            20,
            items.count {
                it.effectiveMelodyId != null
            }
        )

        assertEquals(
            32,
            items.count {
                it.effectiveMelodyId == null
            }
        )

        assertEquals(
            3,
            items.count {
                it.melodyCandidateIds.size > 1
            }
        )

        assertEquals(
            52,
            preview.resolvedItemCount
        )

        assertEquals(
            0,
            preview.blockedItemCount
        )

        assertFalse(
            preview.hasBlockingDiagnostics
        )

        assertTrue(
            preview.prayers.all {
                it.resolvedItems.isNotEmpty()
            }
        )
    }

    @Test
    fun previewPreservesResolvedOccurrenceIdentity() {
        val source = loadSource()

        val full =
            compositionMapper.map(source)

        val preview =
            previewSlice.create(full)

        val fullResolvedIds =
            full.prayers
                .flatMap {
                    it.resolvedItems
                }
                .map {
                    it.id
                }
                .toSet()

        val previewIds =
            preview.prayers
                .flatMap {
                    it.resolvedItems
                }
                .map {
                    it.id
                }
                .toSet()

        assertEquals(
            fullResolvedIds,
            previewIds
        )
    }

    @Test
    fun previewPreservesSourceOrderingAmongIncludedItems() {
        val source = loadSource()

        val full =
            compositionMapper.map(source)

        val preview =
            previewSlice.create(full)

        preview.prayers.forEach { previewPrayer ->
            val fullPrayer =
                full.prayers.single {
                    it.prayerId ==
                            previewPrayer.prayerId
                }

            val expected =
                fullPrayer.orderedSourceItemIds.filter {
                        id ->
                    id in fullPrayer.resolvedItems
                        .map { it.id }
                        .toSet()
                }

            assertEquals(
                expected,
                previewPrayer.orderedSourceItemIds
            )

            assertEquals(
                expected,
                previewPrayer.resolvedItems.map {
                    it.id
                }
            )
        }
    }

    @Test
    fun previewCanProduceValidNavigationProjection() {
        val source = loadSource()

        val full =
            compositionMapper.map(source)

        val preview =
            previewSlice.create(full)

        val navigation =
            navigationMapper.map(
                source = source,
                composition = preview
            )

        assertEquals(
            preview.prayers.map {
                it.prayerId
            },
            navigation.prayerSequences.map {
                it.prayerId
            }
        )

        assertEquals(
            preview.prayers.map {
                it.orderedSourceItemIds
            },
            navigation.prayerSequences.map {
                it.liturgicalItemIds
            }
        )

        assertEquals(
            52,
            navigation.prayerSequences.sumOf {
                it.liturgicalItemIds.size
            }
        )
    }

    @Test
    fun previewCreationIsDeterministic() {
        val source = loadSource()

        val full =
            compositionMapper.map(source)

        assertEquals(
            previewSlice.create(full),
            previewSlice.create(full)
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