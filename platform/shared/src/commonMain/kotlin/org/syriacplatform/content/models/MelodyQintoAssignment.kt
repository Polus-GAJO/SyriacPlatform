package org.syriacplatform.content.models

import org.syriacplatform.common.types.MelodyId
import org.syriacplatform.common.types.QintoId

/**
 * يربط لحنًا بقينة واحدة.
 *
 * تسمح عدة سجلات بارتباط:
 * - القينة الواحدة بعدة ألحان
 * - اللحن الواحد بعدة قينات
 */
data class MelodyQintoAssignment(
    val melodyId: MelodyId,
    val qintoId: QintoId,
    val role: MelodyQintoRole?
)