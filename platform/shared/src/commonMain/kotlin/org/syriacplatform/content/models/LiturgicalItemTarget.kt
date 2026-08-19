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

    /**
     * ظهور Qolo داخل السياق الليتورجي.
     *
     * وجود Qolo في الصلاة مستقل عن حسم اللحن.
     *
     * effectiveMelodyId:
     * - يحتوي Melody واحدة عندما يكون اللحن محسومًا.
     * - null عندما لا يمكن حسم Melody واحدة.
     *
     * melodyCandidateIds:
     * - فارغة عندما لا توجد قينة محددة أو لا توجد مرشحات.
     * - تحتوي المرشحات القانونية عندما توجد أكثر من Melody
     *   ولا يجوز اختيار واحدة منها اعتباطيًا.
     */
    data class Qolo(
        val qoloId: QoloId,
        val effectiveMelodyId: MelodyId? = null,
        val melodyCandidateIds: List<MelodyId> = emptyList(),
        val verses: List<LiturgicalTextRef>
    ) : LiturgicalItemTarget

    /**
     * ظهور Qolo حقيقي داخل السياق الليتورجي
     * لكن هويته canonical لم تُحسم بعد في قاعدة المؤلف.
     *
     * يبقى العنصر الليتورجي وموضعه محفوظين،
     * ويمكن أن يحمل نصوصًا سياقية حتى قبل حسم Qolo.
     */
    data class UnresolvedQolo(
        val verses: List<LiturgicalTextRef>
    ) : LiturgicalItemTarget
}