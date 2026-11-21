package org.syriacplatform.packageformat.loading

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

/**
 * Shhimo-only navigation reader. It does not alter the Occasion package loader.
 * The caller loads canonical content through ApplicationPackageLoader separately.
 */
class ShhimoDailyNavigationLoader(
    private val source: PackageSource,
    private val json: Json = Json { ignoreUnknownKeys = true }
) {
    suspend fun load(): ShhimoDailyNavigation {
        val bytes = requireNotNull(
            source.readBytesOrNull(PackagePaths.BOOK_DAILY_NAVIGATION)
        ) { "Missing Shhimo daily navigation file." }
        val navigation = json.decodeFromString<ShhimoDailyNavigation>(
            bytes.decodeToString()
        )
        require(navigation.bookId > 0L) { "Invalid book ID." }
        require(navigation.days.map { it.dayId } == (1L..7L).toList()) {
            "Shhimo daily navigation must contain days 1 through 7 in order."
        }
        val sequences = navigation.days.flatMap { day ->
            require(day.prayers.isNotEmpty()) { "Day ${day.dayId} has no prayers." }
            require(day.prayers.map { it.prayerId }.distinct().size == day.prayers.size) {
                "Duplicate prayer in day ${day.dayId}."
            }
            day.prayers.map { it.sequenceId }
        }
        require(sequences.all { it > 0L } && sequences.distinct().size == sequences.size) {
            "Invalid or duplicate Shhimo sequence IDs."
        }
        return navigation
    }
}

@Serializable
data class ShhimoDailyNavigation(
    val bookId: Long,
    val days: List<ShhimoDailyNavigationDay>
)

@Serializable
data class ShhimoDailyNavigationDay(
    val dayId: Long,
    val prayers: List<ShhimoDailyNavigationPrayer>
)

@Serializable
data class ShhimoDailyNavigationPrayer(
    val prayerId: Long,
    val sequenceId: Long
)
