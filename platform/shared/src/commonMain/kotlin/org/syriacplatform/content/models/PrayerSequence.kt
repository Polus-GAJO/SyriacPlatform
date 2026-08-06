package org.syriacplatform.content.models

import org.syriacplatform.common.types.LiturgicalItemId
import org.syriacplatform.common.types.PrayerId
import org.syriacplatform.common.types.PrayerSequenceId

/**
 * التحقق السياقي المرتب لصلاة معيّنة.
 *
 * تحتفظ Prayer بهويتها الدائمة، بينما تحدد PrayerSequence
 * العناصر الليتورجية وترتيبها في سياق محدد.
 */
data class PrayerSequence(
    val id: PrayerSequenceId,
    val prayerId: PrayerId,
    val liturgicalItemIds: List<LiturgicalItemId>
)