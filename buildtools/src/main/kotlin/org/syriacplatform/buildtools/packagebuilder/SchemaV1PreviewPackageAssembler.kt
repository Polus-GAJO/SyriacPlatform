package org.syriacplatform.buildtools.packagebuilder

import org.syriacplatform.buildtools.schema.SchemaV1CanonicalContent
import org.syriacplatform.buildtools.schema.SchemaV1CompositionDraft
import org.syriacplatform.buildtools.schema.SchemaV1NavigationContent

class SchemaV1PreviewPackageAssembler {

    fun assemble(
        canonical: SchemaV1CanonicalContent,
        composition: SchemaV1CompositionDraft,
        navigation: SchemaV1NavigationContent
    ): SchemaV1PreviewPackage {
        require(
            !composition.hasBlockingDiagnostics
        ) {
            "Preview package assembly requires a " +
                    "non-blocking composition."
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
                .map { it.qoloId }
                .toSet()

        val melodyIds =
            liturgicalItems
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
                        "org.syriacplatform.preview.occasion1",
                    packageName =
                        "Occasion 1 Development Preview",
                    schemaVersion = "1.0",
                    packageVersion = "0.1.0",
                    contentVersion =
                        "occasion-1-preview",
                    applicationId =
                        "syriacplatform-reference",
                    applicationName =
                        "SyriacPlatform Reference",
                    platform = "generic",
                    defaultLanguage = "syr",
                    profile = "Occasion",
                    minimumCoreVersion = "1.0.0",
                    targetSchemaVersion = "1.0",
                    supportedFeatures =
                        emptyList(),
                    generatedAt =
                        "2026-08-18T00:00:00Z",
                    buildTool =
                        "SyriacPlatform Build Tools",
                    buildVersion =
                        "0.1.0",
                    buildRevision =
                        "working-tree"
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