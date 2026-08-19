package org.syriacplatform.buildtools.packagebuilder

import java.nio.file.Path

/**
 * نتيجة عملية بناء حزمة Occasion واحدة.
 *
 * تمثل النتيجة المادية التي كتبها Build Tools
 * بالإضافة إلى بيانات الحزمة التي أنتجت تلك الملفات.
 */
data class OccasionPackageBuildResult(
    val occasionId: Long,
    val outputDirectory: Path,
    val packageData: SchemaV1PreviewPackage
) {

    val prayerCount: Int
        get() =
            packageData.prayers.size

    val liturgicalItemCount: Int
        get() =
            packageData.liturgicalItems.size
}