package org.syriacplatform.packageformat.loading

import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue
import org.syriacplatform.common.result.Result
import org.syriacplatform.common.types.MediaAssetId
import org.syriacplatform.common.types.Version
import org.syriacplatform.content.models.MediaAsset
import org.syriacplatform.content.models.Melody
import org.syriacplatform.content.runtime.RuntimeContent
import org.syriacplatform.content.runtime.RuntimeContentIndex
import org.syriacplatform.packageformat.dto.MelodyJsonDto
import org.syriacplatform.packageformat.mappers.toDomain
import org.syriacplatform.packagevalidation.compatibility.CoreCompatibility

class CoreMediaIngestionTest {

    private class FakePackageSource(
        private val files: Map<String, ByteArray>
    ) : PackageSource {

        override suspend fun readBytesOrNull(
            path: String
        ): ByteArray? {
            return files[path]
        }
    }

    @Test
    fun loaderDiscoversAndLoadsMediaAssets() =
        runTest {
            val emptyCollection =
                """{"items":[]}""".encodeToByteArray()

            val mediaAssets =
                """
                {
                  "items": [
                    {
                      "id": 7,
                      "type": "AUDIO",
                      "path": "media/audio/melodies/media-000007.mp3"
                    }
                  ]
                }
                """.trimIndent().encodeToByteArray()

            val source =
                FakePackageSource(
                    files = mapOf(
                        PackagePaths.MANIFEST to
                                validManifestJson().encodeToByteArray(),
                        PackagePaths.ENTRY_POINTS to emptyCollection,
                        PackagePaths.OCCASIONS to emptyCollection,
                        PackagePaths.PRAYERS to emptyCollection,
                        PackagePaths.PRAYER_SEQUENCES to emptyCollection,
                        PackagePaths.LITURGICAL_ITEMS to emptyCollection,
                        PackagePaths.TEXTS to emptyCollection,
                        PackagePaths.MEDIA_ASSETS to mediaAssets
                    )
                )

            val loader =
                ApplicationPackageLoader(source)

            val structure =
                loader.discoverStructure()

            assertTrue(
                structure.collectionPresence.mediaAssets
            )

            val result =
                loader.load(
                    CoreCompatibility(
                        version = Version(1, 2, 0),
                        supportedSchemaVersions = setOf("1.0")
                    )
                )

            val success =
                assertIs<PackageLoadResult.Success>(result)

            val asset =
                success.packageData.mediaAssets.single()

            assertEquals(
                MediaAssetId(7L),
                asset.id
            )
            assertEquals(
                "AUDIO",
                asset.type
            )
            assertEquals(
                "media/audio/melodies/media-000007.mp3",
                asset.path
            )
        }

    @Test
    fun melodyRecordingIdsAreIngestedAsTypedIds() {
        val dto =
            Json.decodeFromString<MelodyJsonDto>(
                """
                {
                  "id": 602,
                  "qoloId": 1,
                  "name": "Melody 602",
                  "searchName": "Melody 602",
                  "hasRecording": true,
                  "recordingIds": [292, 293]
                }
                """.trimIndent()
            )

        val mapped =
            assertIs<Result.Success<*>>(
                dto.toDomain()
            )

        val melody =
            mapped.data as Melody

        assertEquals(
            listOf(
                MediaAssetId(292L),
                MediaAssetId(293L)
            ),
            melody.recordingIds
        )
    }

    @Test
    fun legacyMelodyWithoutRecordingIdsRemainsReadable() {
        val dto =
            Json.decodeFromString<MelodyJsonDto>(
                """
                {
                  "id": 31,
                  "qoloId": 1,
                  "name": "Legacy melody",
                  "searchName": "Legacy melody",
                  "hasRecording": true
                }
                """.trimIndent()
            )

        val mapped =
            assertIs<Result.Success<*>>(
                dto.toDomain()
            )

        val melody =
            mapped.data as Melody

        assertTrue(melody.hasRecording)
        assertTrue(melody.recordingIds.isEmpty())
    }

    @Test
    fun absentMediaCollectionRemainsBackwardsCompatible() =
        runTest {
            val loader =
                ApplicationPackageLoader(
                    FakePackageSource(
                        files = mapOf(
                            PackagePaths.MANIFEST to
                                    "{}".encodeToByteArray()
                        )
                    )
                )

            val structure =
                loader.discoverStructure()

            assertFalse(
                structure.collectionPresence.mediaAssets
            )
        }

    @Test
    fun runtimeIndexProvidesMediaAssetLookup() {
        val asset =
            MediaAsset(
                id = MediaAssetId(217L),
                type = "AUDIO",
                path =
                    "media/audio/melodies/media-000217.mp3"
            )

        val content =
            RuntimeContent(
                entryPoints = emptyList(),
                occasions = emptyList(),
                prayers = emptyList(),
                prayerSequences = emptyList(),
                liturgicalItems = emptyList(),
                texts = emptyList(),
                petgomos = emptyList(),
                qolos = emptyList(),
                melodies = emptyList(),
                qintos = emptyList(),
                melodyQintoAssignments = emptyList(),
                mediaAssets = listOf(asset)
            )

        val index =
            RuntimeContentIndex.from(content)

        assertEquals(
            asset,
            index.mediaAssetsById[
                MediaAssetId(217L)
            ]
        )
    }

    private fun validManifestJson(): String {
        return """
            {
              "packageId": "test-package",
              "packageName": "Test Package",
              "schemaVersion": "1.0",
              "packageVersion": "1.0.0",
              "contentVersion": "1.0.0",
              "application": {
                "id": "test-app",
                "name": "Test App",
                "platform": "test",
                "defaultLanguage": "syr"
              },
              "profile": "occasion",
              "build": {
                "generatedAt": "2026-08-26T00:00:00Z",
                "buildTool": "test",
                "buildVersion": "1.0.0",
                "buildRevision": "test"
              },
              "compatibility": {
                "minimumCoreVersion": "1.0.0",
                "targetSchemaVersion": "1.0",
                "supportedFeatures": []
              }
            }
        """.trimIndent()
    }
}