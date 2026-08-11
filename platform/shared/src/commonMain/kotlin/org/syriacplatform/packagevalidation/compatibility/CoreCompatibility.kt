package org.syriacplatform.packagevalidation.compatibility

import org.syriacplatform.common.types.Version

/**
 * يصف قدرات إصدار Core الحالي فيما يتعلق
 * بتوافق Application Packages.
 *
 * لا يمثل بيانات الحزمة نفسها، بل يمثل ما يستطيع
 * هذا الـ Core فهمه وتشغيله.
 */
data class CoreCompatibility(
    val version: Version,
    val supportedSchemaVersions: Set<String>
)