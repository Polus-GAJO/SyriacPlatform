package org.syriacplatform.packagevalidation

/**
 * قاعدة تحقق مستقلة تعمل على نوع محدد من البيانات.
 *
 * كل قاعدة تعيد جميع المشكلات التي اكتشفتها،
 * ولا تتوقف عند أول مشكلة.
 */
fun interface PackageValidationRule<in T> {

    fun validate(
        value: T
    ): List<ValidationIssue>
}