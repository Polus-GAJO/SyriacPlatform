package org.syriacplatform.content.models

import org.syriacplatform.common.types.MediaAssetId

/**
 * Canonical runtime-facing media metadata.
 *
 * path is package-relative.
 *
 * The ingestion layer preserves the package media type as a String.
 * Supported-type validation belongs to the next validation/runtime layer.
 */
data class MediaAsset(
    val id: MediaAssetId,
    val type: String,
    val path: String
)