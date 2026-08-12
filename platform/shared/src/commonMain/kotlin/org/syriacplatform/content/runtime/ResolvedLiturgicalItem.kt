package org.syriacplatform.content.runtime

import org.syriacplatform.content.models.LiturgicalItem
import org.syriacplatform.content.models.Melody
import org.syriacplatform.content.models.Petgomo
import org.syriacplatform.content.models.Qolo
import org.syriacplatform.content.models.TextContent

/**
 * LiturgicalItem بعد حل المراجع القانونية اللازمة لعرضه في Runtime.
 */
data class ResolvedLiturgicalItem(
    val item: LiturgicalItem,
    val target: ResolvedLiturgicalItemTarget
)

/**
 * الهدف الفعلي الجاهز للاستهلاك داخل Runtime.
 */
sealed interface ResolvedLiturgicalItemTarget {

    data class Text(
        val text: TextContent,
        val petgomo: Petgomo?
    ) : ResolvedLiturgicalItemTarget

    data class Qolo(
        val qolo: org.syriacplatform.content.models.Qolo,
        val effectiveMelody: Melody
    ) : ResolvedLiturgicalItemTarget
}