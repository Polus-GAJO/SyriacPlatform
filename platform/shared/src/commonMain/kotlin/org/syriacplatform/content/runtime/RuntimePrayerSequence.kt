package org.syriacplatform.content.runtime

import org.syriacplatform.content.models.Prayer
import org.syriacplatform.content.models.PrayerSequence

/**
 * PrayerSequence جاهزة للاستخدام داخل Runtime.
 *
 * تحفظ ترتيب LiturgicalItems كما هو معرف
 * في PrayerSequence الأصلية.
 */
data class RuntimePrayerSequence(
    val sequence: PrayerSequence,
    val prayer: Prayer,
    val items: List<ResolvedLiturgicalItem>
)