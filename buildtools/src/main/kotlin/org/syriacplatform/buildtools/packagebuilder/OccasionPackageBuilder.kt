package org.syriacplatform.buildtools.packagebuilder

import java.nio.file.Files
import java.nio.file.Path
import org.syriacplatform.buildtools.preview.DevelopmentPreviewSlice
import org.syriacplatform.buildtools.schema.SchemaV1CanonicalMapper
import org.syriacplatform.buildtools.schema.SchemaV1CompositionMapper
import org.syriacplatform.buildtools.schema.SchemaV1NavigationMapper
import org.syriacplatform.buildtools.source.AuthorSourceDataLoader

/**
 * نقطة الدخول الموحدة لبناء حزمة Occasion.
 *
 * تجمع هذه الفئة مراحل Build Tools الحالية في عملية واحدة:
 *
 * source
 *   -> canonical mapping
 *   -> composition mapping
 *   -> development package slice
 *   -> navigation projection
 *   -> package assembly
 *   -> physical package writing
 */
class OccasionPackageBuilder(
    private val loader: AuthorSourceDataLoader =
        AuthorSourceDataLoader(),

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

        require(
            occasionId > 0
        ) {
            "Occasion id must be positive."
        }

        val normalizedSourceDirectory =
            sourceDirectory
                .toAbsolutePath()
                .normalize()

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

        val source =
            loader.load(
                normalizedSourceDirectory
            )

        require(
            source.occasion.id ==
                    occasionId
        ) {
            "Requested Occasion $occasionId " +
                    "does not match source Occasion " +
                    "${source.occasion.id}."
        }

        val canonical =
            canonicalMapper.map(
                source
            )

        val fullComposition =
            compositionMapper.map(
                source
            )

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
                    "does not match requested Occasion " +
                    "$occasionId."
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
                config = config
            )

        /*
         * Build output is regenerated from scratch.
         *
         * This avoids stale optional files from a previous
         * package build remaining inside the output directory.
         */
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
                normalizedOutputDirectory
        )

        return OccasionPackageBuildResult(
            occasionId = occasionId,
            outputDirectory =
                normalizedOutputDirectory,
            packageData = packageData
        )
    }
}