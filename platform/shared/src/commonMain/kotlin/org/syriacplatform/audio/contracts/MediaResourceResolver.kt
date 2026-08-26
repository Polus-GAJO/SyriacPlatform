package org.syriacplatform.audio.contracts

import org.syriacplatform.audio.models.MediaResource
import org.syriacplatform.common.result.Result
import org.syriacplatform.content.models.MediaAsset

/**
 * Boundary between canonical MediaAsset metadata and
 * a platform-playable resource reference.
 *
 * Implementations belong to platform/application integration layers.
 */
interface MediaResourceResolver {

    fun resolve(
        mediaAsset: MediaAsset
    ): Result<MediaResource>
}
