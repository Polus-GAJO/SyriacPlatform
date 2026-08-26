package org.syriacplatform.audio.resources

import org.jetbrains.compose.resources.MissingResourceException
import org.syriacplatform.audio.contracts.MediaResourceResolver
import org.syriacplatform.audio.models.MediaResource
import org.syriacplatform.common.result.Result
import org.syriacplatform.common.types.ErrorCode
import org.syriacplatform.common.types.PlatformError
import org.syriacplatform.content.models.MediaAsset
import org.syriacplatform.resources.Res

/**
 * Resolves canonical package media paths through
 * Compose Multiplatform raw resources.
 *
 * MediaAsset.path is package-relative, for example:
 *
 * media/audio/melodies/media-000293.mp3
 *
 * Compose Resources exposes the same file below:
 *
 * files/media/audio/melodies/media-000293.mp3
 *
 * getUri() is intentionally used instead of readBytes():
 * audio/video files must be handed to the native player as
 * resource references rather than loaded wholly into Core memory.
 */
class ComposeResourceMediaResourceResolver(
    private val uriProvider:
        (String) -> String =
        { resourcePath ->
            Res.getUri(
                resourcePath
            )
        }
) : MediaResourceResolver {

    override fun resolve(
        mediaAsset: MediaAsset
    ): Result<MediaResource> {
        val resourcePath =
            "files/${mediaAsset.path}"

        return try {
            val uri =
                uriProvider(
                    resourcePath
                )

            if (uri.isBlank()) {
                return Result.Failure(
                    PlatformError(
                        code =
                            ErrorCode.RESOURCE_UNAVAILABLE,
                        message =
                            "Resolved media resource URI is blank: " +
                                    resourcePath
                    )
                )
            }

            Result.Success(
                MediaResource(
                    mediaAssetId =
                        mediaAsset.id,
                    uri =
                        uri
                )
            )
        } catch (
            error: MissingResourceException
        ) {
            Result.Failure(
                PlatformError(
                    code =
                        ErrorCode.RESOURCE_NOT_FOUND,
                    message =
                        "Media resource was not found: " +
                                resourcePath,
                    cause =
                        error
                )
            )
        } catch (
            error: Throwable
        ) {
            Result.Failure(
                PlatformError(
                    code =
                        ErrorCode.RESOURCE_UNAVAILABLE,
                    message =
                        "Media resource could not be resolved: " +
                                resourcePath,
                    cause =
                        error
                )
            )
        }
    }
}
