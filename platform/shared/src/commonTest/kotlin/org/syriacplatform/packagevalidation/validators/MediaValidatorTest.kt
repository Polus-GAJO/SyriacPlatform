package org.syriacplatform.packagevalidation.validators

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.syriacplatform.common.types.ErrorCode
import org.syriacplatform.common.types.MediaAssetId
import org.syriacplatform.common.types.MelodyId
import org.syriacplatform.common.types.QoloId
import org.syriacplatform.content.models.MediaAsset
import org.syriacplatform.content.models.Melody
import org.syriacplatform.packagevalidation.PackageValidationTestFixture

class MediaValidatorTest {

    private val validator =
        MediaValidator()

    @Test
    fun acceptsValidCanonicalMediaMetadata() {
        val packageData =
            PackageValidationTestFixture.packageWith(
                melodies =
                    listOf(
                        melody(
                            recordingIds =
                                listOf(
                                    MediaAssetId(7L)
                                )
                        )
                    ),
                mediaAssets =
                    listOf(
                        mediaAsset(
                            id = 7L,
                            path =
                                "media/audio/melodies/media-000007.mp3"
                        )
                    )
            )

        assertTrue(
            validator.validate(packageData).isEmpty()
        )
    }

    @Test
    fun rejectsMissingRecordingReference() {
        val issues =
            validator.validate(
                PackageValidationTestFixture.packageWith(
                    melodies =
                        listOf(
                            melody(
                                recordingIds =
                                    listOf(
                                        MediaAssetId(999L)
                                    )
                            )
                        )
                )
            )

        assertEquals(
            ErrorCode.INVALID_REFERENCE,
            issues.single().code
        )
    }

    @Test
    fun rejectsUnsafeMediaPaths() {
        val unsafePaths =
            listOf(
                "../outside.mp3",
                "/absolute/file.mp3",
                "C:/media/file.mp3",
                "https://example.test/file.mp3",
                "media\\audio\\file.mp3",
                "audio/file.mp3",
                "media/audio/../file.mp3",
                "media//file.mp3"
            )

        unsafePaths.forEachIndexed { index, unsafePath ->
            val issues =
                validator.validate(
                    PackageValidationTestFixture.packageWith(
                        mediaAssets =
                            listOf(
                                mediaAsset(
                                    id = index.toLong() + 1L,
                                    path = unsafePath
                                )
                            )
                    )
                )

            assertTrue(
                issues.any {
                    it.code ==
                            ErrorCode.INVALID_PACKAGE_DATA &&
                            it.location?.endsWith(".path") ==
                            true
                },
                "Expected unsafe path to be rejected: $unsafePath"
            )
        }
    }

    @Test
    fun rejectsHasRecordingMismatch() {
        val issues =
            validator.validate(
                PackageValidationTestFixture.packageWith(
                    melodies =
                        listOf(
                            melody(
                                hasRecording = true,
                                recordingIds = emptyList()
                            )
                        )
                )
            )

        assertTrue(
            issues.any {
                it.location ==
                        "melodies[10].hasRecording"
            }
        )
    }

    @Test
    fun rejectsDuplicateRecordingIdsWithinMelody() {
        val assetId =
            MediaAssetId(7L)

        val issues =
            validator.validate(
                PackageValidationTestFixture.packageWith(
                    melodies =
                        listOf(
                            melody(
                                recordingIds =
                                    listOf(
                                        assetId,
                                        assetId
                                    )
                            )
                        ),
                    mediaAssets =
                        listOf(
                            mediaAsset(
                                id = 7L,
                                path =
                                    "media/audio/melodies/media-000007.mp3"
                            )
                        )
                )
            )

        assertTrue(
            issues.any {
                it.location ==
                        "melodies[10].recordingIds"
            }
        )
    }

    @Test
    fun doesNotInferMediaTypeFromFileExtension() {
        val packageData =
            PackageValidationTestFixture.packageWith(
                melodies =
                    listOf(
                        melody(
                            recordingIds =
                                listOf(
                                    MediaAssetId(466L)
                                )
                        )
                    ),
                mediaAssets =
                    listOf(
                        MediaAsset(
                            id =
                                MediaAssetId(466L),
                            type = "AUDIO",
                            path =
                                "media/audio/melodies/media-000466.mp4"
                        )
                    )
            )

        assertTrue(
            validator.validate(packageData).isEmpty()
        )
    }

    private fun melody(
        hasRecording: Boolean = true,
        recordingIds: List<MediaAssetId>
    ): Melody {
        return Melody(
            id = MelodyId(10L),
            qoloId = QoloId(20L),
            name = "Test Melody",
            searchName = "Test Melody",
            hasRecording = hasRecording,
            recordingIds = recordingIds
        )
    }

    private fun mediaAsset(
        id: Long,
        path: String
    ): MediaAsset {
        return MediaAsset(
            id = MediaAssetId(id),
            type = "AUDIO",
            path = path
        )
    }
}