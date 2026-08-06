package org.syriacplatform.content.models

import org.syriacplatform.common.types.OccasionId

/**
 * الهدف القانوني الذي تبدأ منه الحزمة.
 *
 * يدعم الإصدار 1.0 نقطة دخول من نوع Occasion.
 * يمكن إضافة أنواع أخرى مستقبلًا دون تحويل targetId
 * إلى معرف عديم النوع.
 */
sealed interface EntryPointTarget {

    data class Occasion(
        val occasionId: OccasionId
    ) : EntryPointTarget
}