package org.syriacplatform.buildtools.schema

import org.syriacplatform.buildtools.source.AuthorSourceData

/**
 * One validated prayer slice for the existing Schema-v1 package pipeline.
 * The caller supplies a package-unique sequenceId. This is not yet a
 * standalone application package.
 */
data class BookPrayerPackageSlice(
    val sequence: SchemaV1PrayerSequence,
    val liturgicalItems: List<SchemaV1LiturgicalItem>
)

class BookPrayerPackageSliceBuilder(
    private val compositionMapper: BookPrayerCompositionMapper = BookPrayerCompositionMapper(),
    private val itemMapper: BookPrayerLiturgicalItemMapper = BookPrayerLiturgicalItemMapper(),
    private val sequenceMapper: BookPrayerSequenceMapper = BookPrayerSequenceMapper()
) {
    fun build(
        source: AuthorSourceData,
        bookId: Long,
        dayId: Long,
        prayerId: Long,
        sequenceId: Long
    ): BookPrayerPackageSlice {
        val composition = compositionMapper.map(source, bookId, dayId, prayerId)
        val items = itemMapper.map(source, composition)
        val sequence = sequenceMapper.map(composition, sequenceId)
        require(sequence.liturgicalItemIds == items.map { it.id }) {
            "Prayer sequence and liturgical item order do not match."
        }
        return BookPrayerPackageSlice(sequence, items)
    }
}
