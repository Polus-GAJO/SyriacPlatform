package org.syriacplatform.packageformat

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import org.syriacplatform.common.result.Result
import org.syriacplatform.common.types.ErrorCode
import org.syriacplatform.common.types.PetgomoId
import org.syriacplatform.common.types.TextId
import org.syriacplatform.content.models.LiturgicalItem
import org.syriacplatform.content.models.LiturgicalItemTarget
import org.syriacplatform.content.models.Petgomo
import org.syriacplatform.content.models.TextContent
import org.syriacplatform.packageformat.dto.LiturgicalItemJsonDto
import org.syriacplatform.packageformat.dto.PackageCollectionJsonDto
import org.syriacplatform.packageformat.dto.PetgomoJsonDto
import org.syriacplatform.packageformat.dto.TextContentJsonDto
import org.syriacplatform.packageformat.mappers.toDomain
import org.syriacplatform.common.types.MelodyId
import org.syriacplatform.common.types.QoloId
import org.syriacplatform.content.models.LiturgicalTextRef
import org.syriacplatform.packageformat.dto.LiturgicalTextRefJsonDto

class TextContentAndPetgomoTest {

    private val json = Json {
        ignoreUnknownKeys = true
    }

    @Test
    fun textContentRepresentsOneStanzaWithTranslations() {
        val collection = json.decodeFromString<
                PackageCollectionJsonDto<TextContentJsonDto>
                >(
            textsJson
        )

        val result = collection.items.single().toDomain()
        val text =
            assertIs<Result.Success<TextContent>>(result).data

        assertEquals(
            TextId(1001),
            text.id
        )

        assertEquals(
            "ܫܽܘܒܚܳܐ ܠܰܐܒܳܐ",
            text.syriac
        )

        assertEquals(
            listOf("ar", "en"),
            text.translations.map { translation ->
                translation.language
            }
        )

        assertEquals(
            "المجد للآب",
            text.translations[0].content
        )
    }

    @Test
    fun petgomoMayExistWithoutTranslations() {
        val collection = json.decodeFromString<
                PackageCollectionJsonDto<PetgomoJsonDto>
                >(
            petgomosJson
        )

        val result = collection.items.single().toDomain()
        val petgomo =
            assertIs<Result.Success<Petgomo>>(result).data

        assertEquals(
            PetgomoId(15),
            petgomo.id
        )

        assertEquals(
            "ܫܽܘܒܚܳܐ ܠܰܐܒܳܐ ܘܰܠܒܪܳܐ",
            petgomo.syriac
        )

        assertTrue(
            petgomo.translations.isEmpty()
        )
    }

    @Test
    fun textOccurrenceMayReferencePetgomo() {
        val dto = LiturgicalItemJsonDto(
            id = 501,
            type = "text",
            targetId = 1001,
            petgomoId = 15
        )

        val result = dto.toDomain()
        val item =
            assertIs<Result.Success<LiturgicalItem>>(result).data

        val target =
            assertIs<LiturgicalItemTarget.Text>(item.target)

        assertEquals(
            TextId(1001),
            target.textId
        )

        assertEquals(
            PetgomoId(15),
            target.petgomoId
        )
    }

    @Test
    fun sameTextMayAppearWithoutPetgomo() {
        val dto = LiturgicalItemJsonDto(
            id = 502,
            type = "text",
            targetId = 1001
        )

        val result = dto.toDomain()
        val item =
            assertIs<Result.Success<LiturgicalItem>>(result).data

        val target =
            assertIs<LiturgicalItemTarget.Text>(item.target)

        assertEquals(
            TextId(1001),
            target.textId
        )

        assertEquals(
            null,
            target.petgomoId
        )
    }

    @Test
    fun qoloMayNotDeclarePetgomoDirectly() {
        val dto = LiturgicalItemJsonDto(
            id = 503,
            type = "qolo",
            targetId = 438,
            effectiveMelodyId = 75,
            petgomoId = 15
        )

        val result = dto.toDomain()
        val failure =
            assertIs<Result.Failure>(result)

        assertEquals(
            ErrorCode.INVALID_PACKAGE_DATA,
            failure.error.code
        )

        assertEquals(
            "Qolo liturgical item must not declare top-level petgomoId: 503",
            failure.error.message
        )
    }

    @Test
    fun qoloMapsOrderedVersesWithContextualPetgomo() {
        val dto =
            LiturgicalItemJsonDto(
                id = 504,
                type = "qolo",
                targetId = 438,
                effectiveMelodyId = 75,
                verses = listOf(
                    LiturgicalTextRefJsonDto(
                        textId = 1001,
                        petgomoId = 15
                    ),
                    LiturgicalTextRefJsonDto(
                        textId = 1002
                    ),
                    LiturgicalTextRefJsonDto(
                        textId = 1001,
                        petgomoId = 16
                    )
                )
            )

        val result = dto.toDomain()

        val success =
            assertIs<Result.Success<LiturgicalItem>>(
                result
            )

        val target =
            assertIs<LiturgicalItemTarget.Qolo>(
                success.data.target
            )

        assertEquals(
            QoloId(438),
            target.qoloId
        )

        assertEquals(
            MelodyId(75),
            target.effectiveMelodyId
        )

        assertEquals(
            listOf(
                LiturgicalTextRef(
                    textId = TextId(1001),
                    petgomoId = PetgomoId(15)
                ),
                LiturgicalTextRef(
                    textId = TextId(1002),
                    petgomoId = null
                ),
                LiturgicalTextRef(
                    textId = TextId(1001),
                    petgomoId = PetgomoId(16)
                )
            ),
            target.verses
        )
    }


    private companion object {

        val textsJson = """
            {
              "items": [
                {
                  "id": 1001,
                  "syriac": "ܫܽܘܒܚܳܐ ܠܰܐܒܳܐ",
                  "translations": [
                    {
                      "language": "ar",
                      "content": "المجد للآب"
                    },
                    {
                      "language": "en",
                      "content": "Glory to the Father"
                    }
                  ]
                }
              ]
            }
        """.trimIndent()

        val petgomosJson = """
            {
              "items": [
                {
                  "id": 15,
                  "syriac": "ܫܽܘܒܚܳܐ ܠܰܐܒܳܐ ܘܰܠܒܪܳܐ",
                  "translations": []
                }
              ]
            }
        """.trimIndent()
    }
}