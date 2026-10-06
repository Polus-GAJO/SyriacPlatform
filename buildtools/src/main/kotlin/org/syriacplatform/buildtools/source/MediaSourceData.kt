package org.syriacplatform.buildtools.source

import org.syriacplatform.buildtools.source.models.ExistsInMediaSource
import org.syriacplatform.buildtools.source.models.MediaAssetSource
import org.syriacplatform.buildtools.source.models.MediaSegmentSource
import org.syriacplatform.buildtools.source.models.MediaTimingSetSource
import org.syriacplatform.buildtools.source.models.MelodyMediaSource

data class MediaSourceData(
    val mediaAssets: List<MediaAssetSource>,
    val melodyMedia: List<MelodyMediaSource>,
    val existsInMedia: List<ExistsInMediaSource> = emptyList(),
    val mediaTimingSets: List<MediaTimingSetSource> = emptyList(),
    val mediaSegments: List<MediaSegmentSource> = emptyList()
)