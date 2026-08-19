package org.syriacplatform.buildtools.packagebuilder

import java.nio.file.Files
import java.nio.file.Path
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.syriacplatform.buildtools.preview.DevelopmentPreviewSlice
import org.syriacplatform.buildtools.schema.SchemaV1CanonicalMapper
import org.syriacplatform.buildtools.schema.SchemaV1CompositionMapper
import org.syriacplatform.buildtools.schema.SchemaV1NavigationMapper
import org.syriacplatform.buildtools.source.AuthorSourceDataLoader
import kotlinx.serialization.json.JsonNull

class SchemaV1PackageWriterTest {

    private val loader =
        AuthorSourceDataLoader()

    private val canonicalMapper =
        SchemaV1CanonicalMapper()

    private val compositionMapper =
        SchemaV1CompositionMapper()

    private val previewSlice =
        DevelopmentPreviewSlice()

    private val navigationMapper =
        SchemaV1NavigationMapper()

    private val assembler =
        SchemaV1PreviewPackageAssembler()

    private val writer =
        SchemaV1PackageWriter()

    private val json =
        Json {
            ignoreUnknownKeys = false
        }

    @Test
    fun writesPhysicalSchemaV1DevelopmentPreviewPackage() {
        val source =
            loader.load(
                representativeExportDirectory()
            )

        val canonical =
            canonicalMapper.map(source)

        val fullComposition =
            compositionMapper.map(source)

        val preview =
            previewSlice.create(
                fullComposition
            )

        val navigation =
            navigationMapper.map(
                source = source,
                composition = preview
            )

        val config =
            OccasionPackageBuildConfig
                .developmentPreview(
                    occasionId =
                        source.occasion.id
                )

        val packageData =
            assembler.assemble(
                canonical = canonical,
                composition = preview,
                navigation = navigation,
                config = config
            )

        val output =
            outputDirectory()

        if (Files.exists(output)) {
            output.toFile()
                .deleteRecursively()
        }

        writer.write(
            packageData = packageData,
            outputDirectory = output
        )

        assertTrue(
            Files.isRegularFile(
                output.resolve(
                    "manifest.json"
                )
            )
        )

        assertTrue(
            Files.isDirectory(
                output.resolve("content")
            )
        )

        assertTrue(
            Files.isDirectory(
                output.resolve("media")
            )
        )

        val requiredFiles =
            listOf(
                "entry-points.json",
                "occasions.json",
                "prayers.json",
                "prayer-sequences.json",
                "liturgical-items.json",
                "texts.json",
                "qolos.json",
                "melodies.json"
            )

        requiredFiles.forEach {
                fileName ->

            assertTrue(
                Files.isRegularFile(
                    output
                        .resolve("content")
                        .resolve(fileName)
                ),
                "Expected generated file: $fileName"
            )
        }

        val liturgicalItems =
            readCollection(
                output
                    .resolve("content")
                    .resolve(
                        "liturgical-items.json"
                    )
            )

        assertEquals(
            52,
            liturgicalItems.size
        )

        assertTrue(
            liturgicalItems.all {
                it.jsonObject[
                    "type"
                ]?.jsonPrimitive?.content ==
                        "qolo"
            }
        )

        assertEquals(
            20,
            liturgicalItems.count {
                it.jsonObject[
                    "effectiveMelodyId"
                ] != JsonNull
            }
        )

        assertEquals(
            32,
            liturgicalItems.count {
                it.jsonObject[
                    "effectiveMelodyId"
                ] == JsonNull
            }
        )

        assertEquals(
            3,
            liturgicalItems.count {
                it.jsonObject
                    .getValue(
                        "melodyCandidateIds"
                    )
                    .jsonArray
                    .size > 1
            }
        )

        assertEquals(
            29,
            liturgicalItems.count {
                    item ->

                item.jsonObject[
                    "effectiveMelodyId"
                ] == JsonNull &&
                        item.jsonObject
                            .getValue(
                                "melodyCandidateIds"
                            )
                            .jsonArray
                            .isEmpty()
            }
        )

        val qolos =
            readCollection(
                output
                    .resolve("content")
                    .resolve("qolos.json")
            )

        assertTrue(
            qolos.isNotEmpty()
        )

        val manifest =
            json.parseToJsonElement(
                Files.readString(
                    output.resolve(
                        "manifest.json"
                    )
                )
            ).jsonObject

        val compatibility =
            manifest
                .getValue("compatibility")
                .jsonObject

        assertEquals(
            "1.0.0",
            compatibility[
                "minimumCoreVersion"
            ]?.jsonPrimitive?.content
        )

        assertEquals(
            "1.0",
            compatibility[
                "targetSchemaVersion"
            ]?.jsonPrimitive?.content
        )

        val build =
            manifest
                .getValue("build")
                .jsonObject

        assertEquals(
            "2026-08-18T00:00:00Z",
            build[
                "generatedAt"
            ]?.jsonPrimitive?.content
        )

        assertEquals(
            "working-tree",
            build[
                "buildRevision"
            ]?.jsonPrimitive?.content
        )

        assertEquals(
            "1.0",
            manifest[
                "schemaVersion"
            ]?.jsonPrimitive?.content
        )

        assertEquals(
            "Occasion",
            manifest[
                "profile"
            ]?.jsonPrimitive?.content
        )

        /*
         * Qintos are deliberately absent from this
         * development-preview package.
         */
        assertFalse(
            Files.exists(
                output
                    .resolve("content")
                    .resolve("qintos.json")
            )
        )
    }

    private fun readCollection(
        path: Path
    ) =
        json.parseToJsonElement(
            Files.readString(path)
        )
            .jsonObject
            .getValue("items")
            .jsonArray

    private fun representativeExportDirectory():
            Path {

        return Path.of(
            "..",
            "author-database",
            "samples",
            "mapping-analysis"
        ).toAbsolutePath().normalize()
    }

    private fun outputDirectory():
            Path {

        return Path.of(
            "..",
            "buildtools",
            "build",
            "generated",
            "occasion-1-preview"
        ).toAbsolutePath().normalize()
    }
}