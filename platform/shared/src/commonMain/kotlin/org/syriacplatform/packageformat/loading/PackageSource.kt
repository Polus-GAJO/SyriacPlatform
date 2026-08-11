package org.syriacplatform.packageformat.loading

/**
 * مصدر مجرد لملفات Application Package.
 *
 * يفصل منطق تحميل الحزمة عن مكان تخزينها الفعلي.
 *
 * readBytesOrNull:
 * - يعيد محتوى الملف إذا كان موجودًا.
 * - يعيد null إذا كان الملف غير موجود.
 */
interface PackageSource {

    suspend fun readBytesOrNull(
        path: String
    ): ByteArray?
}