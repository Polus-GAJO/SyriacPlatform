package org.syriacplatform.content.models

import org.syriacplatform.common.types.MelodyId
import org.syriacplatform.common.types.PetgomoId
import org.syriacplatform.common.types.QoloId
import org.syriacplatform.common.types.TextId

/**
 * الهدف القانوني الذي يمثله العنصر الليتورجي.
 *
 * Petgomo مرتبط بظهور Text داخل السياق الليتورجي،
 * وليس بكيان TextContent نفسه.
 */
sealed interface LiturgicalItemTarget {

    data class Text(
        val textId: TextId,
        val petgomoId: PetgomoId? = null
    ) : LiturgicalItemTarget

    data class Qolo(
        val qoloId: QoloId,
        val effectiveMelodyId: MelodyId,
        val verses: List<LiturgicalTextRef>
    ) : LiturgicalItemTarget
}