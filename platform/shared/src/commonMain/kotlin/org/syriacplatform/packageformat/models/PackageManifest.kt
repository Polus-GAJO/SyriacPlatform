package org.syriacplatform.packageformat.models

/**
 * التمثيل القانوني لملف manifest.json داخل Core Engine.
 */
data class PackageManifest(
    val packageId: String,
    val packageName: String,
    val schemaVersion: String,
    val packageVersion: String,
    val contentVersion: String,
    val application: ApplicationInfo,
    val profile: PackageProfile,
    val build: BuildInfo,
    val compatibility: CompatibilityInfo
)