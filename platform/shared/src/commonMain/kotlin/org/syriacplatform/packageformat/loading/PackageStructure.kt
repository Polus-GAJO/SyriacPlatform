package org.syriacplatform.packageformat.loading

import org.syriacplatform.packageformat.parsed.PackageCollectionPresence

/**
 * يمثل نتيجة اكتشاف البنية الفيزيائية للحزمة
 * قبل فك JSON وتحويل DTOs.
 *
 * manifestPresent منفصلة عن collectionPresence لأن manifest
 * ليست Collection بل ملف أساسي للحزمة.
 */
data class PackageStructure(
    val manifestPresent: Boolean,
    val collectionPresence: PackageCollectionPresence
)