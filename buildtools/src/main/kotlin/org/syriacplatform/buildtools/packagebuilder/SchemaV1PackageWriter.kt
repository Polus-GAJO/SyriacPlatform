package org.syriacplatform.buildtools.packagebuilder

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import org.syriacplatform.buildtools.schema.SchemaV1MediaAsset
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.syriacplatform.buildtools.schema.SchemaV1EntryPoint
import org.syriacplatform.buildtools.schema.SchemaV1Melody
import org.syriacplatform.buildtools.schema.SchemaV1Occasion
import org.syriacplatform.buildtools.schema.SchemaV1Petgomo
import org.syriacplatform.buildtools.schema.SchemaV1Prayer
import org.syriacplatform.buildtools.schema.SchemaV1PrayerSequence
import org.syriacplatform.buildtools.schema.SchemaV1Qinto
import org.syriacplatform.buildtools.schema.SchemaV1Qolo
import org.syriacplatform.buildtools.schema.SchemaV1QoloLiturgicalItem
import org.syriacplatform.buildtools.schema.SchemaV1LiturgicalItem
import org.syriacplatform.buildtools.schema.SchemaV1UnresolvedQoloLiturgicalItem
import org.syriacplatform.buildtools.schema.SchemaV1Text
import kotlinx.serialization.ExperimentalSerializationApi

@OptIn(ExperimentalSerializationApi::class)
class SchemaV1PackageWriter {

    private val json =
        Json {
            prettyPrint = true
            prettyPrintIndent = "  "
        }

    fun write(
        packageData: SchemaV1PreviewPackage,
        outputDirectory: Path,
        mediaLibraryRoot: Path? = null,
        sourceMediaAssets: List<SchemaV1MediaAsset> = emptyList()
    ) {
        val contentDirectory =
            outputDirectory.resolve("content")

        val mediaDirectory =
            outputDirectory.resolve("media")

        Files.createDirectories(
            contentDirectory
        )

        Files.createDirectories(
            mediaDirectory
        )

        if (packageData.mediaAssets.isNotEmpty()) {
            require(mediaLibraryRoot != null) {
                "A mediaLibraryRoot is required when the package contains media."
            }

            copyMediaFiles(
                packageData = packageData,
                sourceMediaAssets = sourceMediaAssets,
                mediaLibraryRoot =
                    mediaLibraryRoot,
                outputDirectory =
                    outputDirectory
            )
        }

        writeJson(
            outputDirectory.resolve(
                "manifest.json"
            ),
            manifestJson(packageData.manifest)
        )

        writeCollection(
            contentDirectory,
            "entry-points.json",
            packageData.entryPoints.map(
                ::entryPointJson
            )
        )

        writeCollection(
            contentDirectory,
            "occasions.json",
            packageData.occasions.map(
                ::occasionJson
            )
        )

        writeCollection(
            contentDirectory,
            "prayers.json",
            packageData.prayers.map(
                ::prayerJson
            )
        )

        writeCollection(
            contentDirectory,
            "prayer-sequences.json",
            packageData.prayerSequences.map(
                ::prayerSequenceJson
            )
        )

        writeCollection(
            contentDirectory,
            "liturgical-items.json",
            packageData.liturgicalItems.map(
                ::liturgicalItemJson
            )
        )

        writeCollection(
            contentDirectory,
            "texts.json",
            packageData.texts.map(
                ::textJson
            )
        )

        /*
         * Optional Occasion-profile collections are written
         * when the generated preview actually uses them.
         */
        if (packageData.petgomos.isNotEmpty()) {
            writeCollection(
                contentDirectory,
                "petgomos.json",
                packageData.petgomos.map(
                    ::petgomoJson
                )
            )
        }

        if (packageData.qolos.isNotEmpty()) {
            writeCollection(
                contentDirectory,
                "qolos.json",
                packageData.qolos.map(
                    ::qoloJson
                )
            )
        }

        if (packageData.melodies.isNotEmpty()) {
            writeCollection(
                contentDirectory,
                "melodies.json",
                packageData.melodies.map(
                    ::melodyJson
                )
            )
        }

        if (packageData.mediaAssets.isNotEmpty()) {
            writeCollection(
                contentDirectory,
                "media-assets.json",
                packageData.mediaAssets.map(
                    ::mediaAssetJson
                )
            )
        }

        if (packageData.qintos.isNotEmpty()) {
            writeCollection(
                contentDirectory,
                "qintos.json",
                packageData.qintos.map(
                    ::qintoJson
                )
            )
        }
    }

    private fun manifestJson(
        manifest: SchemaV1PreviewManifest
    ): JsonElement {
        return buildJsonObject {
            put(
                "packageId",
                manifest.packageId
            )

            put(
                "packageName",
                manifest.packageName
            )

            put(
                "schemaVersion",
                manifest.schemaVersion
            )

            put(
                "packageVersion",
                manifest.packageVersion
            )

            put(
                "contentVersion",
                manifest.contentVersion
            )

            put(
                "application",
                buildJsonObject {
                    put(
                        "id",
                        manifest.applicationId
                    )

                    put(
                        "name",
                        manifest.applicationName
                    )

                    put(
                        "platform",
                        manifest.platform
                    )

                    put(
                        "defaultLanguage",
                        manifest.defaultLanguage
                    )
                }
            )

            put(
                "profile",
                manifest.profile
            )

            put(
                "build",
                buildJsonObject {
                    put(
                        "generatedAt",
                        manifest.generatedAt
                    )

                    put(
                        "buildTool",
                        manifest.buildTool
                    )

                    put(
                        "buildVersion",
                        manifest.buildVersion
                    )

                    put(
                        "buildRevision",
                        manifest.buildRevision
                    )
                }
            )

            put(
                "compatibility",
                buildJsonObject {
                    put(
                        "minimumCoreVersion",
                        manifest.minimumCoreVersion
                    )

                    put(
                        "targetSchemaVersion",
                        manifest.targetSchemaVersion
                    )

                    put(
                        "supportedFeatures",
                        buildJsonArray {
                            manifest
                                .supportedFeatures
                                .forEach {
                                    add(
                                        JsonPrimitive(it)
                                    )
                                }
                        }
                    )
                }
            )
        }
    }

    private fun entryPointJson(
        item: SchemaV1EntryPoint
    ): JsonElement {
        return buildJsonObject {
            put("id", item.id)
            put("name", item.name)
            put("type", "occasion")
            put("targetId", item.occasionId)
            put("default", item.isDefault)
        }
    }

    private fun occasionJson(
        item: SchemaV1Occasion
    ): JsonElement {
        return buildJsonObject {
            put("id", item.id)
            put("name", item.name)

            putNullableString(
                "description",
                item.description
            )

            put(
                "prayerSequenceIds",
                longArrayJson(
                    item.prayerSequenceIds
                )
            )
        }
    }

    private fun prayerJson(
        item: SchemaV1Prayer
    ): JsonElement {
        return buildJsonObject {
            put("id", item.id)
            put("name", item.name)

            putNullableString(
                "description",
                item.description
            )
        }
    }

    private fun prayerSequenceJson(
        item: SchemaV1PrayerSequence
    ): JsonElement {
        return buildJsonObject {
            put("id", item.id)
            put("prayerId", item.prayerId)

            put(
                "liturgicalItemIds",
                longArrayJson(
                    item.liturgicalItemIds
                )
            )
        }
    }

    private fun liturgicalItemJson(
        item: SchemaV1LiturgicalItem
    ): JsonElement {

        return when (item) {

            is SchemaV1QoloLiturgicalItem -> {
                buildJsonObject {
                    put("id", item.id)
                    put("type", "qolo")
                    put("targetId", item.qoloId)

                    if (item.effectiveMelodyId != null) {
                        put(
                            "effectiveMelodyId",
                            item.effectiveMelodyId
                        )
                    } else {
                        put(
                            "effectiveMelodyId",
                            JsonNull
                        )
                    }

                    put(
                        "melodyCandidateIds",
                        longArrayJson(
                            item.melodyCandidateIds
                        )
                    )

                    put(
                        "petgomoId",
                        JsonNull
                    )

                    put(
                        "verses",
                        liturgicalVersesJson(
                            item.verses
                        )
                    )
                }
            }

            is SchemaV1UnresolvedQoloLiturgicalItem -> {
                buildJsonObject {
                    put("id", item.id)
                    put("type", "qolo-unresolved")

                    /*
                     * targetId remains physically required
                     * by Schema-v1 JSON DTO.
                     *
                     * Zero here means that Qolo identity
                     * is intentionally unresolved.
                     */
                    put("targetId", 0L)

                    put(
                        "effectiveMelodyId",
                        JsonNull
                    )

                    put(
                        "melodyCandidateIds",
                        JsonArray(emptyList())
                    )

                    put(
                        "petgomoId",
                        JsonNull
                    )

                    put(
                        "verses",
                        liturgicalVersesJson(
                            item.verses
                        )
                    )
                }
            }
        }
    }

    private fun liturgicalVersesJson(
        verses:
        List<
                org.syriacplatform.buildtools.schema
                .SchemaV1LiturgicalTextRef
                >
    ): JsonArray {

        return buildJsonArray {
            verses.forEach { verse ->
                add(
                    buildJsonObject {
                        put(
                            "textId",
                            verse.textId
                        )

                        if (
                            verse.petgomoId != null
                        ) {
                            put(
                                "petgomoId",
                                verse.petgomoId
                            )
                        } else {
                            put(
                                "petgomoId",
                                JsonNull
                            )
                        }
                    }
                )
            }
        }
    }

    private fun textJson(
        item: SchemaV1Text
    ): JsonElement {
        return buildJsonObject {
            put("id", item.id)
            put("syriac", item.syriac)

            put(
                "translations",
                translationsJson(
                    item.translations
                )
            )
        }
    }

    private fun petgomoJson(
        item: SchemaV1Petgomo
    ): JsonElement {
        return buildJsonObject {
            put("id", item.id)
            put("syriac", item.syriac)

            put(
                "translations",
                translationsJson(
                    item.translations
                )
            )
        }
    }

    private fun qoloJson(
        item: SchemaV1Qolo
    ): JsonElement {
        return buildJsonObject {
            put("id", item.id)
            put("groupId", item.groupId)
            put("sort", item.sort)
            put("name", item.name)
            put("searchName", item.searchName)

            putNullableString(
                "poeticMeter",
                item.poeticMeter
            )
        }
    }

    private fun melodyJson(
        item: SchemaV1Melody
    ): JsonElement {
        return buildJsonObject {
            put("id", item.id)
            put("qoloId", item.qoloId)
            put("name", item.name)
            put("searchName", item.searchName)
            put(
                "hasRecording",
                item.hasRecording
            )

            put(
                "recordingIds",
                longArrayJson(
                    item.recordingIds
                )
            )
        }
    }

    private fun mediaAssetJson(
        item: SchemaV1PackageMediaAsset
    ): JsonElement {
        return buildJsonObject {
            put("id", item.id)
            put("type", item.mediaType)
            put("path", item.path)
        }
    }

    private fun qintoJson(
        item: SchemaV1Qinto
    ): JsonElement {
        return buildJsonObject {
            put("id", item.id)
            put("name", item.name)
        }
    }

    private fun translationsJson(
        items:
        List<
                org.syriacplatform.buildtools.schema
                .SchemaV1Translation
                >
    ): JsonArray {
        return buildJsonArray {
            items.forEach { translation ->
                add(
                    buildJsonObject {
                        put(
                            "language",
                            translation.language
                        )

                        put(
                            "content",
                            translation.content
                        )
                    }
                )
            }
        }
    }

    private fun longArrayJson(
        values: List<Long>
    ): JsonArray {
        return buildJsonArray {
            values.forEach {
                add(JsonPrimitive(it))
            }
        }
    }

    private fun kotlinx.serialization.json
    .JsonObjectBuilder.putNullableString(
        key: String,
        value: String?
    ) {
        if (value == null) {
            put(key, JsonNull)
        } else {
            put(key, value)
        }
    }

    private fun writeCollection(
        directory: Path,
        fileName: String,
        items: List<JsonElement>
    ) {
        val root =
            buildJsonObject {
                put(
                    "items",
                    JsonArray(items)
                )
            }

        writeJson(
            directory.resolve(fileName),
            root
        )
    }

    private fun writeJson(
        path: Path,
        element: JsonElement
    ) {
        val text =
            json
                .encodeToString(
                    JsonElement.serializer(),
                    element
                )
                .replace("\r\n", "\n") +
                    "\n"

        Files.writeString(
            path,
            text,
            StandardCharsets.UTF_8
        )
    }

    private fun copyMediaFiles(
        packageData: SchemaV1PreviewPackage,
        sourceMediaAssets: List<SchemaV1MediaAsset>,
        mediaLibraryRoot: Path,
        outputDirectory: Path
    ) {
        val normalizedRoot =
            mediaLibraryRoot
                .toAbsolutePath()
                .normalize()

        require(
            Files.isDirectory(
                normalizedRoot
            )
        ) {
            "Media library root does not exist: $normalizedRoot"
        }

        val sourceById =
            sourceMediaAssets.associateBy {
                it.id
            }

        packageData.mediaAssets.forEach { packageAsset ->
            val sourceAsset =
                sourceById[packageAsset.id]
                    ?: error(
                        "No source MediaAsset was supplied for " +
                                "package MediaAsset ${packageAsset.id}."
                    )

            val sourcePath =
                normalizedRoot
                    .resolve(
                        sourceAsset
                            .sourceRelativePath
                    )
                    .normalize()

            require(
                sourcePath.startsWith(
                    normalizedRoot
                )
            ) {
                "MediaAsset ${sourceAsset.id} resolves outside " +
                        "the media library root."
            }

            require(
                Files.isRegularFile(
                    sourcePath
                )
            ) {
                "MediaAsset ${sourceAsset.id} source file " +
                        "does not exist: $sourcePath"
            }

            val targetPath =
                outputDirectory
                    .resolve(
                        packageAsset.path
                    )
                    .normalize()

            require(
                targetPath.startsWith(
                    outputDirectory
                        .toAbsolutePath()
                        .normalize()
                )
            ) {
                "Package MediaAsset ${packageAsset.id} resolves " +
                        "outside the package output directory."
            }

            Files.createDirectories(
                targetPath.parent
            )

            Files.copy(
                sourcePath,
                targetPath,
                StandardCopyOption.REPLACE_EXISTING
            )
        }
    }
}