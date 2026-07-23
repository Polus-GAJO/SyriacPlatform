package org.syriacplatform.content.dto

import kotlinx.serialization.Serializable
import org.syriacplatform.common.types.QoloId
import org.syriacplatform.content.models.Qolo

@Serializable
data class QoloJsonDto(
    val id: Long,
    val name: String,
    val poeticMeter: String? = null
) {
    fun toDomain(): Qolo {
        return Qolo(
            id = QoloId(id),
            name = name,
            poeticMeter = poeticMeter
        )
    }
}