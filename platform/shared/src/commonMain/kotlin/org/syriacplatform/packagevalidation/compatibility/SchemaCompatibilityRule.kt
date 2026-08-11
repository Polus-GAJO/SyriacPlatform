package org.syriacplatform.packagevalidation.compatibility

import org.syriacplatform.common.types.ErrorCode
import org.syriacplatform.packageformat.models.PackageManifest
import org.syriacplatform.packagevalidation.PackageValidationRule
import org.syriacplatform.packagevalidation.ValidationIssue
import org.syriacplatform.packagevalidation.ValidationSeverity

/**
 * يتحقق من توافق Schema الخاصة بالحزمة مع قدرات الـ Core الحالي.
 *
 * في Compatibility v1:
 *
 * 1. manifest.schemaVersion يجب أن تكون مدعومة من الـ Core.
 * 2. compatibility.targetSchemaVersion يجب أن تساوي schemaVersion.
 *
 * اختلاف targetSchemaVersion عن schemaVersion سيصبح قابلًا للدعم
 * مستقبلًا عند إضافة migration system.
 */
class SchemaCompatibilityRule(
    private val coreCompatibility: CoreCompatibility
) : PackageValidationRule<PackageManifest> {

    override fun validate(
        value: PackageManifest
    ): List<ValidationIssue> {
        return buildList {

            if (
                value.schemaVersion !in
                coreCompatibility.supportedSchemaVersions
            ) {
                add(
                    ValidationIssue(
                        severity = ValidationSeverity.FATAL,
                        code = ErrorCode.INVALID_PACKAGE_DATA,
                        message =
                            "Schema version ${value.schemaVersion} " +
                                    "is not supported by this Core.",
                        location = "manifest.schemaVersion"
                    )
                )
            }

            if (
                value.compatibility.targetSchemaVersion !=
                value.schemaVersion
            ) {
                add(
                    ValidationIssue(
                        severity = ValidationSeverity.FATAL,
                        code = ErrorCode.INVALID_PACKAGE_DATA,
                        message =
                            "Target schema version " +
                                    "${value.compatibility.targetSchemaVersion} " +
                                    "does not match package schema version " +
                                    "${value.schemaVersion}.",
                        location =
                            "manifest.compatibility.targetSchemaVersion"
                    )
                )
            }
        }
    }
}