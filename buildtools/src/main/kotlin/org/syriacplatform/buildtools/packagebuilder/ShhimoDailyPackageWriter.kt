package org.syriacplatform.buildtools.packagebuilder

import java.nio.file.Files
import java.nio.file.Path
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/** Book-profile navigation is written separately from Occasion navigation. */
class ShhimoDailyPackageWriter(
    private val contentWriter: SchemaV1PackageWriter = SchemaV1PackageWriter()
) {
    fun write(bookId: Long, data: ShhimoDailyPackage, output: Path) {
        require(bookId > 0L)
        require(data.content.entryPoints.isEmpty() && data.content.occasions.isEmpty()) {
            "Book packages must not contain Occasion navigation."
        }
        require(data.days.map { it.dayId } == (1L..7L).toList()) {
            "Shhimo daily package requires seven ordered days."
        }
        require(data.days.flatMap { it.prayers }.map { it.sequenceId }.toSet() ==
            data.content.prayerSequences.map { it.id }.toSet()) {
            "Daily navigation and prayer sequences differ."
        }
        contentWriter.write(data.content, output)
        val navigation = buildJsonObject {
            put("bookId", bookId)
            put("days", buildJsonArray {
                data.days.forEach { day ->
                    add(buildJsonObject {
                        put("dayId", day.dayId)
                        put("prayers", buildJsonArray {
                            day.prayers.forEach { prayer ->
                                add(buildJsonObject {
                                    put("prayerId", prayer.prayerId)
                                    put("sequenceId", prayer.sequenceId)
                                })
                            }
                        })
                    })
                }
            })
        }
        Files.writeString(
            output.resolve("content").resolve("book-daily-navigation.json"),
            Json { prettyPrint = true }.encodeToString(
                kotlinx.serialization.json.JsonObject.serializer(), navigation
            )
        )
        // These two collections belong exclusively to the Occasion profile.
        Files.deleteIfExists(output.resolve("content/entry-points.json"))
        Files.deleteIfExists(output.resolve("content/occasions.json"))
    }
}
