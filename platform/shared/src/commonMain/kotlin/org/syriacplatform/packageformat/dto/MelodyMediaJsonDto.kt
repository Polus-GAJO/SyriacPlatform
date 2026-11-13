package org.syriacplatform.packageformat.dto

import kotlinx.serialization.Serializable
import org.syriacplatform.common.types.MediaAssetId
import org.syriacplatform.common.types.MelodyId
import org.syriacplatform.content.models.MelodyMedia

@Serializable
internal data class MelodyMediaJsonDto(
    val id: Long,
    val melodyId: Long,
    val mediaAssetId: Long,
    val role: String,
    val sort: Int
) {
    fun toDomain(): MelodyMedia = MelodyMedia(
        id = id,
        melodyId = MelodyId(melodyId),
        mediaAssetId = MediaAssetId(mediaAssetId),
        role = role,
        sort = sort
    )
}
