package org.syriacplatform.audio.models

import org.syriacplatform.common.types.MediaAssetId

/**
 * Platform-neutral reference to a playable packaged media resource.
 *
 * uri is opaque to Core. Android/iOS implementations decide how
 * a package-relative MediaAsset path becomes a native playable URI.
 */
data class MediaResource(
    val mediaAssetId: MediaAssetId,
    val uri: String
)
