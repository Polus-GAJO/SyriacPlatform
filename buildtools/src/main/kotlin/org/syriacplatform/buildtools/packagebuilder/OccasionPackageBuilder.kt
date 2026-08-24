package org.syriacplatform.buildtools.packagebuilder

import java.nio.file.Files
import java.nio.file.Path
import org.syriacplatform.buildtools.preview.DevelopmentPreviewSlice
import org.syriacplatform.buildtools.schema.SchemaV1CanonicalMapper
import org.syriacplatform.buildtools.schema.SchemaV1CanonicalMedia
import org.syriacplatform.buildtools.schema.SchemaV1CompositionDraft
import org.syriacplatform.buildtools.schema.SchemaV1CompositionMapper
import org.syriacplatform.buildtools.schema.SchemaV1MediaMapper
import org.syriacplatform.buildtools.schema.SchemaV1NavigationMapper
import org.syriacplatform.buildtools.schema.SchemaV1PackageMediaSelector
import org.syriacplatform.buildtools.schema.SchemaV1QoloLiturgicalItem
import org.syriacplatform.buildtools.source.AuthorSourceDataLoader
import org.syriacplatform.buildtools.source.MediaSourceDataLoader

class OccasionPackageBuilder(
    private val loader: AuthorSourceDataLoader =
        AuthorSourceDataLoader(),

    private val mediaLoader: MediaSourceDataLoader =
        MediaSourceDataLoader(),

    private val mediaMapper: SchemaV1MediaMapper =
        SchemaV1MediaMapper(),

    private val mediaSelector: SchemaV1PackageMediaSelector =
        SchemaV1PackageMediaSelector(),

    private val canonicalMapper: SchemaV1CanonicalMapper =
        SchemaV1CanonicalMapper(),

    private val compositionMapper: SchemaV1CompositionMapper =
        SchemaV1CompositionMapper(),

    private val previewSlice: DevelopmentPreviewSlice =
        DevelopmentPreviewSlice(),

    private val navigationMapper: SchemaV1NavigationMapper =
        SchemaV1NavigationMapper(),

    private val assembler: SchemaV1PreviewPackageAssembler =
        SchemaV1PreviewPackageAssembler(),

    private val writer: SchemaV1PackageWriter =
        SchemaV1PackageWriter()
) {

    fun build(
        sourceDirectory: Path,
        occasionId: Long,
        outputDirectory: Path
    ): OccasionPackageBuildResult {
        return buildInternal(
            sourceDirectory = sourceDirectory,
            mediaSourceDirectory = null,
            mediaLibraryRoot = null,
            occasionId = occasionId,
            outputDirectory = outputDirectory
        )
    }

    fun build(
        sourceDirectory: Path,
        mediaSourceDirectory: Path,
        mediaLibraryRoot: Path,
        occasionId: Long,
        outputDirectory: Path
    ): OccasionPackageBuildResult {
        return buildInternal(
            sourceDirectory = sourceDirectory,
            mediaSourceDirectory = mediaSourceDirectory,
            mediaLibraryRoot = mediaLibraryRoot,
            occasionId = occasionId,
            outputDirectory = outputDirectory
        )
    }

    private fun buildInternal(
        sourceDirectory: Path,
        mediaSourceDirectory: Path?,
        mediaLibraryRoot: Path?,
        occasionId: Long,
        outputDirectory: Path
    ): OccasionPackageBuildResult {

        require(occasionId > 0) {
            "Occasion id must be positive."
        }

        val normalizedSourceDirectory =
            sourceDirectory
                .toAbsolutePath()
                .normalize()

        val normalizedMediaSourceDirectory =
            mediaSourceDirectory
                ?.toAbsolutePath()
                ?.normalize()

        val normalizedMediaLibraryRoot =
            mediaLibraryRoot
                ?.toAbsolutePath()
                ?.normalize()

        val normalizedOutputDirectory =
            outputDirectory
                .toAbsolutePath()
                .normalize()

        require(
            normalizedSourceDirectory !=
                    normalizedOutputDirectory
        ) {
            "Source and output directories must be different."
        }

        if (normalizedMediaSourceDirectory != null) {
            require(
                normalizedMediaLibraryRoot != null
            ) {
                "A mediaLibraryRoot is required for a media-aware build."
            }

            require(
                normalizedMediaSourceDirectory !=
                        normalizedOutputDirectory
            ) {
                "Media source and output directories must be different."
            }

            require(
                normalizedMediaLibraryRoot !=
                        normalizedOutputDirectory
            ) {
                "Media library root and output directory must be different."
            }
        }

        val source =
            loader.load(
                normalizedSourceDirectory
            )

        require(
            source.occasion.id == occasionId
        ) {
            "Requested Occasion $occasionId " +
                    "does not match source Occasion " +
                    "${source.occasion.id}."
        }

        val fullComposition =
            compositionMapper.map(source)

        val packageComposition =
            previewSlice.create(
                fullComposition
            )

        require(
            packageComposition.occasionId ==
                    occasionId
        ) {
            "Generated composition Occasion " +
                    "${packageComposition.occasionId} " +
                    "does not match requested Occasion $occasionId."
        }

        val packageMedia: SchemaV1CanonicalMedia =
            if (
                normalizedMediaSourceDirectory != null
            ) {
                mediaSelector.select(
                    canonicalMedia =
                        mediaMapper.map(
                            mediaLoader.load(
                                normalizedMediaSourceDirectory
                            )
                        ),
                    melodyIds =
                        packageMelodyIds(
                            packageComposition
                        )
                )
            } else {
                SchemaV1CanonicalMedia(
                    mediaAssets = emptyList(),
                    melodyMedia = emptyList()
                )
            }

        val canonical =
            if (
                normalizedMediaSourceDirectory != null
            ) {
                canonicalMapper.map(
                    source = source,
                    media = packageMedia
                )
            } else {
                canonicalMapper.map(source)
            }

        val navigation =
            navigationMapper.map(
                source = source,
                composition = packageComposition
            )

        val config =
            OccasionPackageBuildConfig
                .developmentPreview(
                    occasionId = occasionId
                )

        val packageData =
            assembler.assemble(
                canonical = canonical,
                composition = packageComposition,
                navigation = navigation,
                config = config,
                media = packageMedia
            )

        if (
            Files.exists(
                normalizedOutputDirectory
            )
        ) {
            check(
                normalizedOutputDirectory
                    .toFile()
                    .deleteRecursively()
            ) {
                "Could not clear package output directory: " +
                        normalizedOutputDirectory
            }
        }

        writer.write(
            packageData = packageData,
            outputDirectory =
                normalizedOutputDirectory,
            mediaLibraryRoot =
                normalizedMediaLibraryRoot,
            sourceMediaAssets =
                packageMedia.mediaAssets
        )

        return OccasionPackageBuildResult(
            occasionId = occasionId,
            outputDirectory =
                normalizedOutputDirectory,
            packageData = packageData
        )
    }

    private fun packageMelodyIds(
        composition: SchemaV1CompositionDraft
    ): Set<Long> {
        return composition.prayers
            .flatMap { it.resolvedItems }
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
    }
}