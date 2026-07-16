package org.syriacplatform.content.models

import org.syriacplatform.common.types.QoloId

/**
 * يمثل ترتيلة داخل المنصة.
 */
data class Qolo(

    val id: QoloId,

    val name: String,

    val poeticMeter: String?
)