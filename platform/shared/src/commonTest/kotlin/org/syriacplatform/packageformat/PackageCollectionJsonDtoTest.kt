package org.syriacplatform.packageformat

import kotlinx.serialization.SerializationException
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import org.syriacplatform.packageformat.dto.PackageCollectionJsonDto

class PackageCollectionJsonDtoTest {

    private val json = Json {
        ignoreUnknownKeys = true
    }

    @Test
    fun wrappedCollectionIsDecoded() {
        val collection =
            json.decodeFromString<
                    PackageCollectionJsonDto<ExampleItemJsonDto>
                    >(
                """
                {
                  "items": [
                    {
                      "id": 1,
                      "name": "First"
                    },
                    {
                      "id": 2,
                      "name": "Second"
                    }
                  ]
                }
                """.trimIndent()
            )

        assertEquals(
            2,
            collection.items.size
        )

        assertEquals(
            ExampleItemJsonDto(
                id = 1,
                name = "First"
            ),
            collection.items[0]
        )

        assertEquals(
            ExampleItemJsonDto(
                id = 2,
                name = "Second"
            ),
            collection.items[1]
        )
    }

    @Test
    fun emptyWrappedCollectionIsDecoded() {
        val collection =
            json.decodeFromString<
                    PackageCollectionJsonDto<ExampleItemJsonDto>
                    >(
                """
                {
                  "items": []
                }
                """.trimIndent()
            )

        assertEquals(
            emptyList(),
            collection.items
        )
    }

    @Test
    fun itemOrderIsPreserved() {
        val collection =
            json.decodeFromString<
                    PackageCollectionJsonDto<ExampleItemJsonDto>
                    >(
                """
                {
                  "items": [
                    {
                      "id": 3,
                      "name": "Third"
                    },
                    {
                      "id": 1,
                      "name": "First"
                    },
                    {
                      "id": 2,
                      "name": "Second"
                    }
                  ]
                }
                """.trimIndent()
            )

        assertEquals(
            listOf(3L, 1L, 2L),
            collection.items.map { item ->
                item.id
            }
        )
    }

    @Test
    fun bareJsonArrayIsRejected() {
        assertFailsWith<SerializationException> {
            json.decodeFromString<
                    PackageCollectionJsonDto<ExampleItemJsonDto>
                    >(
                """
                [
                  {
                    "id": 1,
                    "name": "First"
                  }
                ]
                """.trimIndent()
            )
        }
    }

    @Test
    fun missingItemsPropertyIsRejected() {
        assertFailsWith<SerializationException> {
            json.decodeFromString<
                    PackageCollectionJsonDto<ExampleItemJsonDto>
                    >(
                """
                {
                  "values": [
                    {
                      "id": 1,
                      "name": "First"
                    }
                  ]
                }
                """.trimIndent()
            )
        }
    }

    @Test
    fun unknownWrapperPropertiesAreIgnored() {
        val collection =
            json.decodeFromString<
                    PackageCollectionJsonDto<ExampleItemJsonDto>
                    >(
                """
                {
                  "generatedBy": "Test",
                  "items": [
                    {
                      "id": 1,
                      "name": "First"
                    }
                  ]
                }
                """.trimIndent()
            )

        assertEquals(
            listOf(1L),
            collection.items.map { item ->
                item.id
            }
        )
    }

    @Serializable
    private data class ExampleItemJsonDto(
        val id: Long,
        val name: String
    )
}