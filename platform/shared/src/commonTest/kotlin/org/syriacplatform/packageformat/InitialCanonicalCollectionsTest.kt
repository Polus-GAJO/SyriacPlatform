package org.syriacplatform.packageformat

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue
import org.syriacplatform.common.result.Result
import org.syriacplatform.common.types.ErrorCode
import org.syriacplatform.common.types.OccasionId
import org.syriacplatform.common.types.PrayerSequenceId
import org.syriacplatform.content.models.EntryPoint
import org.syriacplatform.content.models.EntryPointTarget
import org.syriacplatform.content.models.Occasion
import org.syriacplatform.content.models.Prayer
import org.syriacplatform.packageformat.dto.EntryPointJsonDto
import org.syriacplatform.packageformat.dto.OccasionJsonDto
import org.syriacplatform.packageformat.dto.PackageCollectionJsonDto
import org.syriacplatform.packageformat.dto.PrayerJsonDto
import org.syriacplatform.packageformat.mappers.toDomain

class InitialCanonicalCollectionsTest {

    private val json = Json {
        ignoreUnknownKeys = true
    }

    @Test
    fun entryPointCollectionIsDecodedAndMapped() {
        val collection = json.decodeFromString<
                PackageCollectionJsonDto<EntryPointJsonDto>
                >(
            entryPointsJson
        )

        assertEquals(
            2,
            collection.items.size
        )

        val firstResult = collection.items[0].toDomain()
        val first =
            assertIs<Result.Success<EntryPoint>>(firstResult).data

        assertEquals(
            1L,
            first.id.value
        )

        assertEquals(
            "Nativity",
            first.name
        )

        assertTrue(first.isDefault)

        val target =
            assertIs<EntryPointTarget.Occasion>(first.target)

        assertEquals(
            OccasionId(101),
            target.occasionId
        )

        val secondResult = collection.items[1].toDomain()
        val second =
            assertIs<Result.Success<EntryPoint>>(secondResult).data

        assertFalse(second.isDefault)
    }

    @Test
    fun unsupportedEntryPointTypeProducesFailure() {
        val dto = EntryPointJsonDto(
            id = 1,
            name = "Unsupported",
            type = "unknown",
            targetId = 101,
            isDefault = false
        )

        val result = dto.toDomain()
        val failure =
            assertIs<Result.Failure>(result)

        assertEquals(
            ErrorCode.UNEXPECTED_ENTITY_TYPE,
            failure.error.code
        )

        assertEquals(
            "Unsupported entry point type: unknown",
            failure.error.message
        )
    }

    @Test
    fun occasionCollectionPreservesPrayerSequenceOrder() {
        val collection = json.decodeFromString<
                PackageCollectionJsonDto<OccasionJsonDto>
                >(
            occasionsJson
        )

        val result = collection.items.single().toDomain()
        val occasion =
            assertIs<Result.Success<Occasion>>(result).data

        assertEquals(
            101L,
            occasion.id.value
        )

        assertEquals(
            "Nativity",
            occasion.name
        )

        assertEquals(
            "Demonstration occasion.",
            occasion.description
        )

        assertEquals(
            listOf(
                PrayerSequenceId(303),
                PrayerSequenceId(301),
                PrayerSequenceId(302)
            ),
            occasion.prayerSequenceIds
        )
    }

    @Test
    fun prayerCollectionIsDecodedAndMapped() {
        val collection = json.decodeFromString<
                PackageCollectionJsonDto<PrayerJsonDto>
                >(
            prayersJson
        )

        assertEquals(
            2,
            collection.items.size
        )

        val eveningResult =
            collection.items[0].toDomain()

        val evening =
            assertIs<Result.Success<Prayer>>(eveningResult).data

        assertEquals(
            201L,
            evening.id.value
        )

        assertEquals(
            "Evening Prayer",
            evening.name
        )

        assertEquals(
            "The evening prayer.",
            evening.description
        )

        val morningResult =
            collection.items[1].toDomain()

        val morning =
            assertIs<Result.Success<Prayer>>(morningResult).data

        assertEquals(
            202L,
            morning.id.value
        )

        assertEquals(
            "Morning Prayer",
            morning.name
        )

        assertNull(morning.description)
    }

    private companion object {

        val entryPointsJson = """
            {
              "items": [
                {
                  "id": 1,
                  "name": "Nativity",
                  "type": "occasion",
                  "targetId": 101,
                  "default": true
                },
                {
                  "id": 2,
                  "name": "Another Occasion",
                  "type": "occasion",
                  "targetId": 102,
                  "default": false
                }
              ]
            }
        """.trimIndent()

        val occasionsJson = """
            {
              "items": [
                {
                  "id": 101,
                  "name": "Nativity",
                  "description": "Demonstration occasion.",
                  "prayerSequenceIds": [
                    303,
                    301,
                    302
                  ]
                }
              ]
            }
        """.trimIndent()

        val prayersJson = """
            {
              "items": [
                {
                  "id": 201,
                  "name": "Evening Prayer",
                  "description": "The evening prayer."
                },
                {
                  "id": 202,
                  "name": "Morning Prayer"
                }
              ]
            }
        """.trimIndent()
    }
}