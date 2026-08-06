package org.syriacplatform.content.models

import org.syriacplatform.common.types.MelodyId
import org.syriacplatform.common.types.QoloId

/**
 * لحن قانوني من ألحان قولو معين.
 *
 * اسم اللحن هو وسيلة بشرية للتعرف عليه،
 * ولا يحدد القينة أو الموضع الليتورجي بذاته.
 */
data class Melody(
    val id: MelodyId,
    val qoloId: QoloId,
    val name: String,
    val searchName: String,
    val hasRecording: Boolean
)