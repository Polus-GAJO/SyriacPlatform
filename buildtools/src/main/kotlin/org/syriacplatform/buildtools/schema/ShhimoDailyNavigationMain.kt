package org.syriacplatform.buildtools.schema

import java.nio.file.Files
import java.nio.file.Path
import org.syriacplatform.buildtools.source.AuthorSourceMapper
import org.syriacplatform.buildtools.source.CsvTableReader

/** Prints daily Shhimo navigation from the actual Access ExistsIn export. */
fun main(args: Array<String>) {
    require(args.size == 2) { "Usage: <bookId> <sourceDirectory>" }
    val bookId = args[0].toLong()
    val path = Path.of(args[1]).resolve("ExistsIn.csv")
    require(Files.isRegularFile(path)) { "Missing ExistsIn.csv: $path" }
    val mapper = AuthorSourceMapper()
    val items = CsvTableReader().read(path).rows.map(mapper::toExistsIn)
    val days = ShhimoDailyNavigationMapper().map(items, bookId)
    println("Book $bookId daily navigation: ${days.size} days")
    var totalPrayers = 0
    var totalItems = 0
    for (day in days) {
        println("Day ${day.dayId}: ${day.prayers.size} prayers")
        for (prayer in day.prayers) {
            println("  Prayer ${prayer.prayerId}: ${prayer.itemCount} items")
            totalPrayers++
            totalItems += prayer.itemCount
        }
    }
    println("Total: $totalPrayers day/prayer groups, $totalItems items")
    val excluded = items.count { it.bookId == bookId && it.dayId == 0L }
    println("Excluded DayN=0 items: $excluded (reserved for separate navigation)")
}
