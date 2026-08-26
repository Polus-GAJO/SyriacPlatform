package org.syriacplatform.audio.resources

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import org.jetbrains.compose.resources.MissingResourceException
import org.syriacplatform.audio.models.MediaResource
import org.syriacplatform.common.result.Result
import org.syriacplatform.common.types.ErrorCode
import org.syriacplatform.common.types.MediaAssetId
import org.syriacplatform.content.models.MediaAsset

class ComposeResourceMediaResourceResolverTest {

    @Test
    fun mapsCanonicalPackagePathToComposeFilesPath() {
        var requestedPath: String? =
            null

        val resolver =
            ComposeResourceMediaResourceResolver(
                uriProvider = { path ->
                    requestedPath =
                        path

                    "compose-resource://$path"
                }
            )

        val result =
            assertIs<Result.Success<MediaResource>>(
                resolver.resolve(
                    mediaAsset(
                        id = 293L,
                        path =
                            "media/audio/melodies/media-000293.mp3"
                    )
                )
            )

        assertEquals(
            "files/media/audio/melodies/media-000293.mp3",
            requestedPath
        )

        assertEquals(
            MediaAssetId(293L),
            result.data.mediaAssetId
        )

        assertEquals(
            "compose-resource://" +
                    "files/media/audio/melodies/media-000293.mp3",
            result.data.uri
        )
    }

    @Test
    fun preservesCanonicalPathWithoutInferringTypeFromExtension() {
        var requestedPath: String? =
            null

        val resolver =
            ComposeResourceMediaResourceResolver(
                uriProvider = { path ->
                    requestedPath =
                        path

                    "compose-resource://$path"
                }
            )

        val result =
            resolver.resolve(
                MediaAsset(
                    id =
                        MediaAssetId(466L),
                    type =
                        "AUDIO",
                    path =
                        "media/audio/melodies/media-000466.mp4"
                )
            )

        assertIs<Result.Success<MediaResource>>(
            result
        )

        assertEquals(
            "files/media/audio/melodies/media-000466.mp4",
            requestedPath
        )
    }

    @Test
    fun missingComposeResourceReturnsResourceNotFound() {
        val resolver =
            ComposeResourceMediaResourceResolver(
                uriProvider = {
                    throw MissingResourceException(
                        "missing test resource"
                    )
                }
            )

        val result =
            assertIs<Result.Failure>(
                resolver.resolve(
                    mediaAsset(
                        id = 7L,
                        path =
                            "media/audio/melodies/missing.mp3"
                    )
                )
            )

        assertEquals(
            ErrorCode.RESOURCE_NOT_FOUND,
            result.error.code
        )
    }

    @Test
    fun blankResolvedUriReturnsResourceUnavailable() {
        val resolver =
            ComposeResourceMediaResourceResolver(
                uriProvider = {
                    ""
                }
            )

        val result =
            assertIs<Result.Failure>(
                resolver.resolve(
                    mediaAsset(
                        id = 8L,
                        path =
                            "media/audio/melodies/media-000008.mp3"
                    )
                )
            )

        assertEquals(
            ErrorCode.RESOURCE_UNAVAILABLE,
            result.error.code
        )
    }

    @Test
    fun unexpectedResolutionFailureReturnsResourceUnavailable() {
        val resolver =
            ComposeResourceMediaResourceResolver(
                uriProvider = {
                    error(
                        "test failure"
                    )
                }
            )

        val result =
            assertIs<Result.Failure>(
                resolver.resolve(
                    mediaAsset(
                        id = 9L,
                        path =
                            "media/audio/melodies/media-000009.mp3"
                    )
                )
            )

        assertEquals(
            ErrorCode.RESOURCE_UNAVAILABLE,
            result.error.code
        )
    }

    private fun mediaAsset(
        id: Long,
        path: String
    ): MediaAsset {
        return MediaAsset(
            id =
                MediaAssetId(id),
            type =
                "AUDIO",
            path =
                path
        )
    }
}
