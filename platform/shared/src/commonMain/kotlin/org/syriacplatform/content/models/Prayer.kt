package org.syriacplatform.content.models

import org.syriacplatform.common.types.PrayerId

/**
 * الهوية الدائمة لصلاة معروفة.
 *
 * لا تحتوي Prayer على العناصر الليتورجية؛
 * فالمحتوى السياقي المرتب يعود إلى PrayerSequence.
 */
data class Prayer(
    val id: PrayerId,
    val name: String,
    val description: String?
)