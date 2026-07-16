package org.syriacplatform.common.types

/**
 * الأصل المشترك لجميع المعرّفات الدائمة في SyriacPlatform.
 *
 * القيمة تمثل هوية الكيان، ولا تعبّر عن ترتيبه.
 */
interface PlatformId {
    val value: Long
}