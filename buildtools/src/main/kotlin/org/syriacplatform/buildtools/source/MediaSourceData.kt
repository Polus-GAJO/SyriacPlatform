package org.syriacplatform.buildtools.source

import org.syriacplatform.buildtools.source.models.MediaAssetSource
import org.syriacplatform.buildtools.source.models.MelodyMediaSource

data class MediaSourceData(
    val mediaAssets: List<MediaAssetSource>,
    val melodyMedia: List<MelodyMediaSource>
)