package org.syriacplatform.packageformat.parsed

/**
 * يسجل وجود مجموعات المحتوى فعليًا في الحزمة الأصلية.
 *
 * هذه المعلومات مستقلة عن عدد العناصر داخل المجموعة:
 *
 * present = true + empty list
 * تعني أن المجموعة موجودة في الحزمة ولكنها فارغة.
 *
 * present = false + empty list
 * تعني أن المجموعة غير موجودة أصلًا في الحزمة.
 *
 * تستخدم هذه المعلومات خصوصًا في Profile Validation،
 * حيث يمكن أن يفرض Profile وجود مجموعة معينة حتى لو كانت فارغة.
 */
data class PackageCollectionPresence(
    val entryPoints: Boolean,
    val occasions: Boolean,
    val prayers: Boolean,
    val prayerSequences: Boolean,
    val liturgicalItems: Boolean,
    val texts: Boolean,
    val qolos: Boolean,
    val melodies: Boolean,
    val qintos: Boolean,
    val petgomos: Boolean,
    val melodyQintoAssignments: Boolean
)