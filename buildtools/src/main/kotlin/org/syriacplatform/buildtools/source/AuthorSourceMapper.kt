package org.syriacplatform.buildtools.source

import org.syriacplatform.buildtools.source.models.ExistsInSource
import org.syriacplatform.buildtools.source.models.ExistsInTextSource
import org.syriacplatform.buildtools.source.models.MelodySource
import org.syriacplatform.buildtools.source.models.OccaExisSource
import org.syriacplatform.buildtools.source.models.OccasionSource
import org.syriacplatform.buildtools.source.models.PetExisSource
import org.syriacplatform.buildtools.source.models.PetgomoSource
import org.syriacplatform.buildtools.source.models.PrayerSource
import org.syriacplatform.buildtools.source.models.QintoSource
import org.syriacplatform.buildtools.source.models.QoloSource
import org.syriacplatform.buildtools.source.models.TextSource

class AuthorSourceMapper {

    fun toOccasion(row: CsvRow): OccasionSource {
        return OccasionSource(
            sort = row.optionalLong("OccSort"),
            id = row.requiredLong("OccN"),
            name = row["Occasion"],
            day = row.optionalDouble("OccDay"),
            monthId = row.optionalLong("OccMonth")
        )
    }

    fun toPrayer(row: CsvRow): PrayerSource {
        return PrayerSource(
            id = row.requiredLong("PrayerN"),
            name = row["Prayer"]
        )
    }

    fun toExistsIn(row: CsvRow): ExistsInSource {
        return ExistsInSource(
            id = row.requiredLong("ID"),
            sort = row.optionalLong("Sort"),
            bookId = row.optionalLong("BookN"),
            prayerId = row.optionalLong("PrayerN"),
            locationId = row.optionalLong("LocationN"),
            qoloId = row.optionalLong("QoloN"),
            qintoId = row.optionalLong("QintoN"),
            noteId = row.optionalLong("NoteN"),
            dayId = row.optionalLong("DayN")
        )
    }

    fun toOccaExis(row: CsvRow): OccaExisSource {
        return OccaExisSource(
            id = row.requiredLong("OccaExisID"),
            existsInId = row.optionalLong("ExistInID"),
            occasionId = row.optionalLong("OccN")
        )
    }

    fun toExistsInText(row: CsvRow): ExistsInTextSource {
        return ExistsInTextSource(
            id = row.requiredLong("ID"),
            textId = row.optionalLong("TextID"),
            existsInId = row.optionalLong("ExistsInID"),
            sortInPrayer = row.optionalInt("SortInPra")
        )
    }

    fun toPetExis(row: CsvRow): PetExisSource {
        return PetExisSource(
            id = row.requiredLong("PetExistID"),
            petgomoId = row.optionalLong("PetN"),
            textId = row.optionalLong("TextID"),
            existsInTextId = row.optionalLong("ExistInTextID")
        )
    }

    fun toQolo(row: CsvRow): QoloSource {
        return QoloSource(
            groupId = row.optionalLong("GroupN"),
            id = row.requiredLong("QoloN"),
            sort = row.optionalLong("Sort"),
            searchName = row["QoloSerch"],
            name = row["Qolo"],
            poeticMeter = row["Poetic"]
        )
    }

    fun toText(row: CsvRow): TextSource {
        return TextSource(
            id = row.requiredLong("TextID"),
            syriac = row["TheText"],
            chosen = row.optionalBoolean("Chose"),
            searchText = row["TextSearch"],
            searchKey = row["TextSearchKey"],
            similarityKey = row["TextSimilarityKey"]
        )
    }

    fun toPetgomo(row: CsvRow): PetgomoSource {
        return PetgomoSource(
            id = row.requiredLong("PetN"),
            abcd = row.optionalLong("ABCD"),
            syriac = row["Petgomo"],
            searchText = row["PetSerch"]
        )
    }

    fun toMelody(row: CsvRow): MelodySource {
        return MelodySource(
            id = row.requiredLong("MelodyN"),
            qoloId = row.optionalLong("QoloN"),
            name = row["Melody"],
            searchName = row["MelodySerch"],
            qintoId = row.optionalLong("QintoN"),
            occasionId = row.optionalLong("OccasionN"),
            noteId = row.optionalLong("NoteN"),
            hasRecording = row.optionalBoolean("Record")
        )
    }

    fun toQinto(row: CsvRow): QintoSource {
        return QintoSource(
            id = row.requiredLong("QintoN"),
            name = row["Qinto"]
        )
    }

    private fun CsvRow.requiredLong(
        columnName: String
    ): Long {
        val rawValue = this[columnName]
            ?: error(
                "Required column '$columnName' is null or empty."
            )

        return rawValue.toLongOrNull()
            ?: error(
                "Column '$columnName' must contain a Long, " +
                        "but was '$rawValue'."
            )
    }

    private fun CsvRow.optionalLong(
        columnName: String
    ): Long? {
        val rawValue = this[columnName]
            ?: return null

        return rawValue.toLongOrNull()
            ?: error(
                "Column '$columnName' must contain a Long, " +
                        "but was '$rawValue'."
            )
    }

    private fun CsvRow.optionalInt(
        columnName: String
    ): Int? {
        val rawValue = this[columnName]
            ?: return null

        return rawValue.toIntOrNull()
            ?: error(
                "Column '$columnName' must contain an Int, " +
                        "but was '$rawValue'."
            )
    }

    private fun CsvRow.optionalDouble(
        columnName: String
    ): Double? {
        val rawValue = this[columnName]
            ?: return null

        return rawValue.toDoubleOrNull()
            ?: error(
                "Column '$columnName' must contain a Double, " +
                        "but was '$rawValue'."
            )
    }

    private fun CsvRow.optionalBoolean(
        columnName: String
    ): Boolean? {
        val rawValue = this[columnName]
            ?: return null

        return when (rawValue.lowercase()) {
            "true" -> true
            "false" -> false

            else -> error(
                "Column '$columnName' must contain true or false, " +
                        "but was '$rawValue'."
            )
        }
    }
}