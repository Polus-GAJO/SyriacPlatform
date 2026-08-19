package org.syriacplatform.buildtools.packagebuilder

import org.syriacplatform.buildtools.schema.SchemaV1CanonicalContent
import org.syriacplatform.buildtools.schema.SchemaV1CompositionDraft
import org.syriacplatform.buildtools.schema.SchemaV1NavigationContent
import org.syriacplatform.buildtools.schema.SchemaV1QoloLiturgicalItem
import org.syriacplatform.buildtools.schema.SchemaV1UnresolvedQoloLiturgicalItem

class SchemaV1PreviewPackageAssembler {

    fun assemble(
        canonical: SchemaV1CanonicalContent,
        composition: SchemaV1CompositionDraft,
        navigation: SchemaV1NavigationContent,
        config: OccasionPackageBuildConfig
    ): SchemaV1PreviewPackage {
        require(
            !composition.hasBlockingDiagnostics
        ) {
            "Preview package assembly requires a " +
                    "non-blocking composition."
        }

        require(
            composition.occasionId ==
                    config.occasionId
        ) {
            "Composition Occasion ${composition.occasionId} " +
                    "does not match build configuration Occasion " +
                    "${config.occasionId}."
        }

        val liturgicalItems =
            composition.prayers
                .flatMap { it.resolvedItems }

        val prayerIds =
            navigation.prayerSequences
                .map { it.prayerId }
                .toSet()

        val qoloIds =
            liturgicalItems
                .filterIsInstance<
                        SchemaV1QoloLiturgicalItem
                        >()
                .map { it.qoloId }
                .toSet()

        val melodyIds =
            liturgicalItems
                .filterIsInstance<
                        SchemaV1QoloLiturgicalItem
                        >()
                .flatMap { item ->
                    buildList {
                        item.effectiveMelodyId?.let {
                            add(it)
                        }

                        addAll(
                            item.melodyCandidateIds
                        )
                    }
                }
                .toSet()

        val textIds =
            liturgicalItems
                .flatMap { item ->
                    item.verses.map { it.textId }
                }
                .toSet()

        val petgomoIds =
            liturgicalItems
                .flatMap { item ->
                    item.verses.mapNotNull {
                        it.petgomoId
                    }
                }
                .toSet()

        val prayers =
            canonical.prayers.filter {
                it.id in prayerIds
            }

        val qolos =
            canonical.qolos.filter {
                it.id in qoloIds
            }

        val melodies =
            canonical.melodies.filter {
                it.id in melodyIds
            }

        val texts =
            canonical.texts.filter {
                it.id in textIds
            }

        val petgomos =
            canonical.petgomos.filter {
                it.id in petgomoIds
            }

        require(
            prayers.size == prayerIds.size
        ) {
            "Not every Prayer referenced by navigation " +
                    "exists in canonical content."
        }

        require(
            qolos.size == qoloIds.size
        ) {
            "Not every Qolo referenced by LiturgicalItems " +
                    "exists in canonical content."
        }

        require(
            melodies.size == melodyIds.size
        ) {
            "Not every Melody referenced by LiturgicalItems " +
                    "exists in canonical content."
        }

        require(
            texts.size == textIds.size
        ) {
            "Not every contextual Text referenced by " +
                    "LiturgicalItems exists in canonical content."
        }

        require(
            petgomos.size == petgomoIds.size
        ) {
            "Not every contextual Petgomo referenced by " +
                    "LiturgicalItems exists in canonical content."
        }

        return SchemaV1PreviewPackage(
            manifest =
                SchemaV1PreviewManifest(
                    packageId =
                        config.packageId,
                    packageName =
                        config.packageName,
                    schemaVersion = "1.0",
                    packageVersion =
                        config.packageVersion,
                    contentVersion =
                        config.contentVersion,
                    applicationId =
                        config.applicationId,
                    applicationName =
                        config.applicationName,
                    platform =
                        config.platform,
                    defaultLanguage =
                        config.defaultLanguage,
                    profile = "Occasion",
                    minimumCoreVersion = "1.0.0",
                    targetSchemaVersion = "1.0",
                    supportedFeatures =
                        emptyList(),
                    generatedAt =
                        config.generatedAt,
                    buildTool =
                        config.buildTool,
                    buildVersion =
                        config.buildVersion,
                    buildRevision =
                        config.buildRevision
                ),

            entryPoints =
                navigation.entryPoints,

            occasions =
                navigation.occasions,

            prayers =
                prayers,

            prayerSequences =
                navigation.prayerSequences,

            liturgicalItems =
                liturgicalItems,

            texts =
                texts,

            petgomos =
                petgomos,

            qolos =
                qolos,

            melodies =
                melodies,

            /*
 * Qinto is not required by the Occasion profile
 * for this development package.
 *
 * Melody resolution state is preserved directly
 * on each Qolo LiturgicalItem.
 */
            qintos =
                emptyList()
        )
    }
}