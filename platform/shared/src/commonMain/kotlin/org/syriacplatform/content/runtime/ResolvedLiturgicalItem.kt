package org.syriacplatform.content.runtime

import org.syriacplatform.content.models.LiturgicalItem
import org.syriacplatform.content.models.Melody
import org.syriacplatform.content.models.Petgomo
import org.syriacplatform.content.models.Qolo as ContentQolo
import org.syriacplatform.content.models.TextContent

/**
 * LiturgicalItem بعد حل المراجع القانونية اللازمة لعرضه في Runtime.
 */
data class ResolvedLiturgicalItem(
    val item: LiturgicalItem,
    val target: ResolvedLiturgicalItemTarget
)

/**
 * بيت نصي بعد حل مراجع ظهوره الليتورجي.
 *
 * يمكن أن يظهر TextContent نفسه عدة مرات داخل
 * الترتيلة، وكل ظهور يحتفظ بـ Petgomo الخاصة به.
 */
data class ResolvedLiturgicalText(
    val text: TextContent,
    val petgomo: Petgomo?
)

/**
 * الهدف الفعلي الجاهز للاستهلاك داخل Runtime.
 */
sealed interface ResolvedLiturgicalItemTarget {

    /**
     * نص مستقل داخل التسلسل الليتورجي.
     */
    data class Text(
        val text: TextContent,
        val petgomo: Petgomo?
    ) : ResolvedLiturgicalItemTarget

    /**
     * ظهور ترتيلة Qolo داخل السياق الليتورجي.
     *
     * effectiveMelody تكون موجودة فقط عندما يكون
     * اللحن محسومًا بصورة قانونية.
     *
     * melodyCandidates تحفظ المرشحين عندما توجد
     * أكثر من Melody ولا يجوز اختيار واحدة اعتباطيًا.
     *
     * verses تحفظ الأبيات المحلولة بالترتيب نفسه
     * المحدد في LiturgicalItemTarget.Qolo.
     */
    data class Qolo(
        val qolo: ContentQolo,
        val effectiveMelody: Melody?,
        val melodyCandidates: List<Melody>,
        val verses: List<ResolvedLiturgicalText>
    ) : ResolvedLiturgicalItemTarget
}