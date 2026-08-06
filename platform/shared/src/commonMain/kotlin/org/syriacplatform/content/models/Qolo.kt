package org.syriacplatform.content.models

import org.syriacplatform.common.types.GroupId
import org.syriacplatform.common.types.QoloId

/**
 * الهوية القانونية للقولو.
 *
 * القولو ليس حاوية للحن واحد أو نص واحد،
 * بل كيان دائم يربط عالم النصوص بعالم الألحان.
 */
data class Qolo(
    val id: QoloId,
    val groupId: GroupId,
    val sort: Long,
    val name: String,
    val searchName: String,
    val poeticMeter: String?
)