package org.syriacplatform.packageformat.dto

import kotlinx.serialization.Serializable

/**
 * البنية الفيزيائية لملف manifest.json.
 *
 * هذا النموذج يمثل JSON فقط، ولا يستخدم مباشرة
 * من خدمات Core Engine.
 */
@Serializable
internal data class PackageManifestJsonDto(
    val packageId: String,
    val packageName: String,
    val schemaVersion: String,
    val packageVersion: String,
    val contentVersion: String,
    val application: ApplicationInfoJsonDto,
    val profile: String,
    val build: BuildInfoJsonDto,
    val compatibility: CompatibilityInfoJsonDto
)

@Serializable
internal data class ApplicationInfoJsonDto(
    val id: String,
    val name: String,
    val platform: String,
    val defaultLanguage: String
)

@Serializable
internal data class BuildInfoJsonDto(
    val generatedAt: String,
    val buildTool: String,
    val buildVersion: String,
    val buildRevision: String
)

@Serializable
internal data class CompatibilityInfoJsonDto(
    val minimumCoreVersion: String,
    val targetSchemaVersion: String,
    val supportedFeatures: List<String>
)