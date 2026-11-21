package org.syriacplatform.buildtools.schema

import java.nio.file.Files
import java.nio.file.Path
import org.syriacplatform.buildtools.source.AuthorSourceMapper
import org.syriacplatform.buildtools.source.CsvTableReader
import org.syriacplatform.buildtools.source.models.ExistsInTextSource

/**
 * Checks every daily Shhimo sequence against the Access CSV occurrence order.
 * This is a preflight, not yet a Schema-v1 book package writer.
 */
fun main(args: Array<String>) {
    require(args.size == 2) { "Usage: <bookId> <sourceDirectory>" }
    val bookId = args[0].toLong()
    val directory = Path.of(args[1])
    val csv = CsvTableReader()
    val mapper = AuthorSourceMapper()
    fun read(name: String) = directory.resolve(name).let {
        require(Files.isRegularFile(it)) { "Missing $name: $it" }
        csv.read(it).rows
    }
    val items = read("ExistsIn.csv").map(mapper::toExistsIn)
    val verses = read("ExistsInText.csv").map(mapper::toExistsInText)
    val plan = ShhimoDailySequencePlanner().plan(items, bookId)
    require(plan.isNotEmpty()) { "No daily prayers for book $bookId." }
    require(plan.map { it.sequenceId }.distinct().size == plan.size) {
        "Duplicate prayer sequence IDs."
    }
    val selector = BookPrayerItemSelector()
    val versesByItem = verses.groupBy { it.existsInId }
    var itemCount = 0
    var verseCount = 0
    val seenItems = mutableSetOf<Long>()
    for (entry in plan) {
        val selected = selector.select(items, bookId, entry.dayId, entry.prayerId)
        require(selected.size == entry.itemCount) {
            "Count mismatch for day ${entry.dayId} prayer ${entry.prayerId}."
        }
        for (item in selected) {
            require(seenItems.add(item.id)) { "ExistsIn ${item.id} occurs in multiple daily prayers." }
            val ordered = versesByItem[item.id].orEmpty()
                .sortedWith(compareBy<ExistsInTextSource>({ it.sortInPrayer ?: Int.MAX_VALUE }, { it.id }))
            require(ordered.map { it.id }.distinct().size == ordered.size) {
                "Duplicate verse occurrence in ExistsIn ${item.id}."
            }
            verseCount += ordered.size
        }
        itemCount += selected.size
    }
    println("Shhimo daily preflight OK: ${plan.size} unique prayer sequences, $itemCount items, $verseCount verse occurrences")
    println("Excluded DayN=0: ${items.count { it.bookId == bookId && it.dayId == 0L }} items")
}
