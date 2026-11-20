package org.syriacplatform.buildtools.schema

/**
 * Adapts a book/day/prayer projection to the existing Schema-v1 navigation
 * contract. Sequence IDs belong to the caller's package-wide ID policy:
 * PrayerN alone is not unique across days.
 *
 * The corresponding LiturgicalItems must be emitted by package composition
 * using the same ordered ExistsIn IDs before this sequence can be published.
 */
class BookPrayerSequenceMapper {
    fun map(
        composition: BookPrayerComposition,
        sequenceId: Long
    ): SchemaV1PrayerSequence {
        require(sequenceId > 0L) { "Prayer sequence ID must be positive." }
        require(composition.prayerId > 0L) { "Prayer ID must be positive." }
        val itemIds = composition.items.map { it.existsInId }
        require(itemIds.distinct().size == itemIds.size) {
            "A prayer sequence cannot repeat an ExistsIn occurrence ID."
        }
        return SchemaV1PrayerSequence(
            id = sequenceId,
            prayerId = composition.prayerId,
            liturgicalItemIds = itemIds
        )
    }
}
