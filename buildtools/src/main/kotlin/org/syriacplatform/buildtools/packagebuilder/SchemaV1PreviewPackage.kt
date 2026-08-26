package org.syriacplatform.buildtools.packagebuilder

import org.syriacplatform.buildtools.schema.SchemaV1EntryPoint
import org.syriacplatform.buildtools.schema.SchemaV1Melody
import org.syriacplatform.buildtools.schema.SchemaV1Occasion
import org.syriacplatform.buildtools.schema.SchemaV1Petgomo
import org.syriacplatform.buildtools.schema.SchemaV1Prayer
import org.syriacplatform.buildtools.schema.SchemaV1PrayerSequence
import org.syriacplatform.buildtools.schema.SchemaV1Qinto
import org.syriacplatform.buildtools.schema.SchemaV1Qolo
import org.syriacplatform.buildtools.schema.SchemaV1Text
import org.syriacplatform.buildtools.schema.SchemaV1LiturgicalItem

data class SchemaV1PreviewPackage(
    val manifest: SchemaV1PreviewManifest,
    val entryPoints: List<SchemaV1EntryPoint>,
    val occasions: List<SchemaV1Occasion>,
    val prayers: List<SchemaV1Prayer>,
    val prayerSequences: List<SchemaV1PrayerSequence>,
    val liturgicalItems: List<SchemaV1LiturgicalItem>,
    val texts: List<SchemaV1Text>,
    val petgomos: List<SchemaV1Petgomo>,
    val qolos: List<SchemaV1Qolo>,
    val melodies: List<SchemaV1Melody>,
    val qintos: List<SchemaV1Qinto>,
    val mediaAssets: List<SchemaV1PackageMediaAsset> = emptyList()
)

data class SchemaV1PackageMediaAsset(
    val id: Long,
    val mediaType: String,
    val path: String,
    val performer: String? = null
)

data class SchemaV1PreviewManifest(
    val packageId: String,
    val packageName: String,
    val schemaVersion: String,
    val packageVersion: String,
    val contentVersion: String,
    val applicationId: String,
    val applicationName: String,
    val platform: String,
    val defaultLanguage: String,
    val profile: String,
    val minimumCoreVersion: String,
    val targetSchemaVersion: String,
    val supportedFeatures: List<String>,
    val generatedAt: String,
    val buildTool: String,
    val buildVersion: String,
    val buildRevision: String
)