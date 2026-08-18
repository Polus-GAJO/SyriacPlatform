package org.syriacplatform.packageformat

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import org.syriacplatform.common.result.Result
import org.syriacplatform.common.types.ErrorCode
import org.syriacplatform.common.types.LiturgicalItemId
import org.syriacplatform.common.types.MelodyId
import org.syriacplatform.common.types.PrayerId
import org.syriacplatform.common.types.QoloId
import org.syriacplatform.common.types.TextId
import org.syriacplatform.content.models.LiturgicalItem
import org.syriacplatform.content.models.LiturgicalItemTarget
import org.syriacplatform.content.models.PrayerSequence
import org.syriacplatform.packageformat.dto.LiturgicalItemJsonDto
import org.syriacplatform.packageformat.dto.PackageCollectionJsonDto
import org.syriacplatform.packageformat.dto.PrayerSequenceJsonDto
import org.syriacplatform.packageformat.mappers.toDomain
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PrayerSequenceAndLiturgicalItemTest {

    private val json = Json {
        ignoreUnknownKeys = true
    }

    @Test
    fun prayerSequencePreservesLiturgicalItemOrder() {
        val collection = json.decodeFromString<
                PackageCollectionJsonDto<PrayerSequenceJsonDto>
                >(
            prayerSequencesJson
        )

        val result = collection.items.single().toDomain()
        val sequence =
            assertIs<Result.Success<PrayerSequence>>(result).data

        assertEquals(
            301L,
            sequence.id.value
        )

        assertEquals(
            PrayerId(201),
            sequence.prayerId
        )

        assertEquals(
            listOf(
                LiturgicalItemId(503),
                LiturgicalItemId(501),
                LiturgicalItemId(502)
            ),
            sequence.liturgicalItemIds
        )
    }

    @Test
    fun textLiturgicalItemIsDecodedAndMapped() {
        val collection = decodeLiturgicalItems()

        val result = collection.items[0].toDomain()
        val item =
            assertIs<Result.Success<LiturgicalItem>>(result).data

        assertEquals(
            501L,
            item.id.value
        )

        val target =
            assertIs<LiturgicalItemTarget.Text>(item.target)

        assertEquals(
            TextId(1001),
            target.textId
        )
    }

    @Test
    fun qoloLiturgicalItemIsDecodedWithEffectiveMelody() {
        val collection = decodeLiturgicalItems()

        val result = collection.items[1].toDomain()
        val item =
            assertIs<Result.Success<LiturgicalItem>>(result).data

        assertEquals(
            502L,
            item.id.value
        )

        val target =
            assertIs<LiturgicalItemTarget.Qolo>(item.target)

        assertEquals(
            QoloId(438),
            target.qoloId
        )

        assertEquals(
            MelodyId(75),
            target.effectiveMelodyId
        )
    }

    @Test
    fun qoloWithoutEffectiveMelodyIsAccepted() {
        val dto =
            LiturgicalItemJsonDto(
                id = 502L,
                type = "qolo",
                targetId = 801L,
                effectiveMelodyId = null,
                petgomoId = null,
                verses = emptyList()
            )

        val result =
            dto.toDomain()

        val success =
            assertIs<Result.Success<LiturgicalItem>>(
                result
            )

        val target =
            assertIs<LiturgicalItemTarget.Qolo>(
                success.data.target
            )

        assertEquals(
            QoloId(801L),
            target.qoloId
        )

        assertNull(
            target.effectiveMelodyId
        )

        assertTrue(
            target.melodyCandidateIds.isEmpty()
        )
    }

    @Test
    fun textWithEffectiveMelodyProducesFailure() {
        val dto = LiturgicalItemJsonDto(
            id = 505,
            type = "text",
            targetId = 1001,
            effectiveMelodyId = 75
        )

        val result = dto.toDomain()
        val failure =
            assertIs<Result.Failure>(result)

        assertEquals(
            ErrorCode.INVALID_PACKAGE_DATA,
            failure.error.code
        )

        assertEquals(
            "Text liturgical item must not declare effectiveMelodyId: 505",
            failure.error.message
        )
    }

    @Test
    fun unsupportedLiturgicalItemTypeProducesFailure() {
        val dto = LiturgicalItemJsonDto(
            id = 506,
            type = "reading",
            targetId = 1200
        )

        val result = dto.toDomain()
        val failure =
            assertIs<Result.Failure>(result)

        assertEquals(
            ErrorCode.UNEXPECTED_ENTITY_TYPE,
            failure.error.code
        )

        assertEquals(
            "Unsupported liturgical item type: reading",
            failure.error.message
        )
    }

    @Test
    fun qoloWithAmbiguousMelodyCandidatesIsAccepted() {
        val dto =
            LiturgicalItemJsonDto(
                id = 503L,
                type = "qolo",
                targetId = 116L,
                effectiveMelodyId = null,
                melodyCandidateIds =
                    listOf(
                        119L,
                        1965L
                    ),
                petgomoId = null,
                verses = emptyList()
            )

        val result =
            dto.toDomain()

        val success =
            assertIs<Result.Success<LiturgicalItem>>(
                result
            )

        val target =
            assertIs<LiturgicalItemTarget.Qolo>(
                success.data.target
            )

        assertEquals(
            QoloId(116L),
            target.qoloId
        )

        assertNull(
            target.effectiveMelodyId
        )

        assertEquals(
            listOf(
                MelodyId(119L),
                MelodyId(1965L)
            ),
            target.melodyCandidateIds
        )
    }

    private fun decodeLiturgicalItems():
            PackageCollectionJsonDto<LiturgicalItemJsonDto> {
        return json.decodeFromString(
            liturgicalItemsJson
        )
    }

    private companion object {

        val prayerSequencesJson = """
            {
              "items": [
                {
                  "id": 301,
                  "prayerId": 201,
                  "liturgicalItemIds": [
                    503,
                    501,
                    502
                  ]
                }
              ]
            }
        """.trimIndent()

        val liturgicalItemsJson = """
            {
              "items": [
                {
                  "id": 501,
                  "type": "text",
                  "targetId": 1001
                },
                {
                  "id": 502,
                  "type": "qolo",
                  "targetId": 438,
                  "effectiveMelodyId": 75
                }
              ]
            }
        """.trimIndent()
    }
}