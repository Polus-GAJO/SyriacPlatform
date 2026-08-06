package org.syriacplatform.content.models

import org.syriacplatform.common.types.QintoId

/**
 * قينة ليتورجية أو تصنيف لحني قانوني.
 */
data class Qinto(
    val id: QintoId,
    val name: String
)