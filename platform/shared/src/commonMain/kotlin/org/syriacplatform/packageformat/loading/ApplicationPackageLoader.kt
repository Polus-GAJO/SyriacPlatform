package org.syriacplatform.packageformat.loading

import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import org.syriacplatform.common.result.Result
import org.syriacplatform.common.types.ErrorCode
import org.syriacplatform.common.types.PlatformError
import org.syriacplatform.packageformat.dto.EntryPointJsonDto
import org.syriacplatform.packageformat.dto.LiturgicalItemJsonDto
import org.syriacplatform.packageformat.dto.MediaAssetJsonDto
import org.syriacplatform.packageformat.dto.MelodyJsonDto
import org.syriacplatform.packageformat.dto.MelodyQintoAssignmentJsonDto
import org.syriacplatform.packageformat.dto.OccasionJsonDto
import org.syriacplatform.packageformat.dto.PackageCollectionJsonDto
import org.syriacplatform.packageformat.dto.PackageManifestJsonDto
import org.syriacplatform.packageformat.dto.PetgomoJsonDto
import org.syriacplatform.packageformat.dto.PrayerJsonDto
import org.syriacplatform.packageformat.dto.PrayerSequenceJsonDto
import org.syriacplatform.packageformat.dto.QintoJsonDto
import org.syriacplatform.packageformat.dto.QoloJsonDto
import org.syriacplatform.packageformat.dto.TextContentJsonDto
import org.syriacplatform.packageformat.mappers.toDomain
import org.syriacplatform.packageformat.models.PackageManifest
import org.syriacplatform.packageformat.parsed.PackageCollectionPresence
import org.syriacplatform.packageformat.parsed.ParsedApplicationPackage
import org.syriacplatform.packagevalidation.PackageValidator
import org.syriacplatform.packagevalidation.compatibility.CoreCompatibility

/**
 * ط§ظ„ظ…ظ†ط³ظ‚ ط§ظ„ط£ط¹ظ„ظ‰ ظ„طھط­ظ…ظٹظ„ Application Package.
 *
 * ط§ظ„ظ…ط³ط§ط± ط§ظ„طھط´ط؛ظٹظ„ظٹ ط§ظ„ظƒط§ظ…ظ„:
 *
 * PackageSource
 * â†’ read canonical files once
 * â†’ structure / collection presence
 * â†’ JSON DTO decoding
 * â†’ DTO-to-Domain mapping
 * â†’ ParsedApplicationPackage
 * â†’ PackageValidator
 * â†’ PackageLoadResult
 */
class ApplicationPackageLoader(
    private val source: PackageSource,
    private val json: Json = Json {
        ignoreUnknownKeys = true
    }
) {

    /**
     * ط§ظ„ظ…ط³ط§ط± ط§ظ„طھط´ط؛ظٹظ„ظٹ ط§ظ„ط±ط¦ظٹط³ظٹ.
     *
     * طھظ‚ط±ط£ ط¬ظ…ظٹط¹ ط§ظ„ظ…ظ„ظپط§طھ ط§ظ„ظ‚ط§ظ†ظˆظ†ظٹط© ظ…ط±ط© ظˆط§ط­ط¯ط© ظپظ‚ط·.
     */
    suspend fun load(
        coreCompatibility: CoreCompatibility
    ): PackageLoadResult {
        val files =
            readCanonicalFiles()

        val structure =
            buildStructure(files)

        val manifestResult =
            parseManifest(
                bytes = files.manifest
            )

        val manifest =
            when (manifestResult) {
                is Result.Success ->
                    manifestResult.data

                is Result.Failure ->
                    return PackageLoadResult.Failure(
                        manifestResult.error
                    )
            }

        val entryPoints =
            when (
                val result =
                    parseCollection<EntryPointJsonDto, org.syriacplatform.content.models.EntryPoint>(
                        bytes = files.entryPoints,
                        path = PackagePaths.ENTRY_POINTS,
                        mapper = { dto ->
                            dto.toDomain()
                        }
                    )
            ) {
                is Result.Success ->
                    result.data

                is Result.Failure ->
                    return PackageLoadResult.Failure(
                        result.error
                    )
            }

        val occasions =
            when (
                val result =
                    parseCollection<OccasionJsonDto, org.syriacplatform.content.models.Occasion>(
                        bytes = files.occasions,
                        path = PackagePaths.OCCASIONS,
                        mapper = { dto ->
                            dto.toDomain()
                        }
                    )
            ) {
                is Result.Success ->
                    result.data

                is Result.Failure ->
                    return PackageLoadResult.Failure(
                        result.error
                    )
            }

        val prayers =
            when (
                val result =
                    parseCollection<PrayerJsonDto, org.syriacplatform.content.models.Prayer>(
                        bytes = files.prayers,
                        path = PackagePaths.PRAYERS,
                        mapper = { dto ->
                            dto.toDomain()
                        }
                    )
            ) {
                is Result.Success ->
                    result.data

                is Result.Failure ->
                    return PackageLoadResult.Failure(
                        result.error
                    )
            }

        val prayerSequences =
            when (
                val result =
                    parseCollection<PrayerSequenceJsonDto, org.syriacplatform.content.models.PrayerSequence>(
                        bytes = files.prayerSequences,
                        path = PackagePaths.PRAYER_SEQUENCES,
                        mapper = { dto ->
                            dto.toDomain()
                        }
                    )
            ) {
                is Result.Success ->
                    result.data

                is Result.Failure ->
                    return PackageLoadResult.Failure(
                        result.error
                    )
            }

        val liturgicalItems =
            when (
                val result =
                    parseCollection<LiturgicalItemJsonDto, org.syriacplatform.content.models.LiturgicalItem>(
                        bytes = files.liturgicalItems,
                        path = PackagePaths.LITURGICAL_ITEMS,
                        mapper = { dto ->
                            dto.toDomain()
                        }
                    )
            ) {
                is Result.Success ->
                    result.data

                is Result.Failure ->
                    return PackageLoadResult.Failure(
                        result.error
                    )
            }

        val texts =
            when (
                val result =
                    parseCollection<TextContentJsonDto, org.syriacplatform.content.models.TextContent>(
                        bytes = files.texts,
                        path = PackagePaths.TEXTS,
                        mapper = { dto ->
                            dto.toDomain()
                        }
                    )
            ) {
                is Result.Success ->
                    result.data

                is Result.Failure ->
                    return PackageLoadResult.Failure(
                        result.error
                    )
            }

        val petgomos =
            when (
                val result =
                    parseCollection<PetgomoJsonDto, org.syriacplatform.content.models.Petgomo>(
                        bytes = files.petgomos,
                        path = PackagePaths.PETGOMOS,
                        mapper = { dto ->
                            dto.toDomain()
                        }
                    )
            ) {
                is Result.Success ->
                    result.data

                is Result.Failure ->
                    return PackageLoadResult.Failure(
                        result.error
                    )
            }

        val qolos =
            when (
                val result =
                    parseCollection<QoloJsonDto, org.syriacplatform.content.models.Qolo>(
                        bytes = files.qolos,
                        path = PackagePaths.QOLOS,
                        mapper = { dto ->
                            dto.toDomain()
                        }
                    )
            ) {
                is Result.Success ->
                    result.data

                is Result.Failure ->
                    return PackageLoadResult.Failure(
                        result.error
                    )
            }

        val melodies =
            when (
                val result =
                    parseCollection<MelodyJsonDto, org.syriacplatform.content.models.Melody>(
                        bytes = files.melodies,
                        path = PackagePaths.MELODIES,
                        mapper = { dto ->
                            dto.toDomain()
                        }
                    )
            ) {
                is Result.Success ->
                    result.data

                is Result.Failure ->
                    return PackageLoadResult.Failure(
                        result.error
                    )
            }

        val qintos =
            when (
                val result =
                    parseCollection<QintoJsonDto, org.syriacplatform.content.models.Qinto>(
                        bytes = files.qintos,
                        path = PackagePaths.QINTOS,
                        mapper = { dto ->
                            dto.toDomain()
                        }
                    )
            ) {
                is Result.Success ->
                    result.data

                is Result.Failure ->
                    return PackageLoadResult.Failure(
                        result.error
                    )
            }

        val melodyQintoAssignments =
            when (
                val result =
                    parseCollection<MelodyQintoAssignmentJsonDto, org.syriacplatform.content.models.MelodyQintoAssignment>(
                        bytes =
                            files.melodyQintoAssignments,
                        path =
                            PackagePaths.MELODY_QINTO_ASSIGNMENTS,
                        mapper = { dto ->
                            dto.toDomain()
                        }
                    )
            ) {
                is Result.Success ->
                    result.data

                is Result.Failure ->
                    return PackageLoadResult.Failure(
                        result.error
                    )
            }
        val mediaAssets =

            when (

                val result =

                    parseCollection<MediaAssetJsonDto, org.syriacplatform.content.models.MediaAsset>(

                        bytes = files.mediaAssets,

                        path = PackagePaths.MEDIA_ASSETS,

                        mapper = { dto ->

                            dto.toDomain()

                        }

                    )

            ) {

                is Result.Success ->

                    result.data



                is Result.Failure ->

                    return PackageLoadResult.Failure(

                        result.error

                    )

            }


        val packageData =
            ParsedApplicationPackage(
                manifest = manifest,
                collectionPresence =
                    structure.collectionPresence,
                entryPoints = entryPoints,
                occasions = occasions,
                prayers = prayers,
                prayerSequences = prayerSequences,
                liturgicalItems = liturgicalItems,
                texts = texts,
                petgomos = petgomos,
                qolos = qolos,
                melodies = melodies,
                qintos = qintos,
                melodyQintoAssignments =
                    melodyQintoAssignments,
                mediaAssets = mediaAssets
            )

        val validationReport =
            PackageValidator(
                coreCompatibility =
                    coreCompatibility
            ).validate(
                packageData
            )

        return if (validationReport.isValid) {
            PackageLoadResult.Success(
                packageData = packageData,
                validationReport =
                    validationReport
            )
        } else {
            PackageLoadResult.ValidationFailed(
                packageData = packageData,
                validationReport =
                    validationReport
            )
        }
    }

    /**
     * API طھط´ط®ظٹطµظٹط© ظ„ط§ظƒطھط´ط§ظپ ط§ظ„ط¨ظ†ظٹط© ط§ظ„ظپظٹط²ظٹط§ط¦ظٹط© ظپظ‚ط·.
     */
    suspend fun discoverStructure(): PackageStructure {
        val files =
            readCanonicalFiles()

        return buildStructure(files)
    }

    /**
     * API طھط´ط®ظٹطµظٹط© ظ„طھط­ظ…ظٹظ„ ط§ظ„ظ€ Manifest ظپظ‚ط·.
     */
    suspend fun loadManifest(): Result<PackageManifest> {
        val bytes =
            source.readBytesOrNull(
                PackagePaths.MANIFEST
            )

        return parseManifest(bytes)
    }

    private fun buildStructure(
        files: PackageFileSet
    ): PackageStructure {
        return PackageStructure(
            manifestPresent =
                files.manifest != null,
            collectionPresence =
                PackageCollectionPresence(
                    entryPoints =
                        files.entryPoints != null,
                    occasions =
                        files.occasions != null,
                    prayers =
                        files.prayers != null,
                    prayerSequences =
                        files.prayerSequences != null,
                    liturgicalItems =
                        files.liturgicalItems != null,
                    texts =
                        files.texts != null,
                    petgomos =
                        files.petgomos != null,
                    qolos =
                        files.qolos != null,
                    melodies =
                        files.melodies != null,
                    qintos =
                        files.qintos != null,
                    melodyQintoAssignments =
                        files.melodyQintoAssignments != null,
                    mediaAssets =
                        files.mediaAssets != null
                )
        )
    }

    private fun parseManifest(
        bytes: ByteArray?
    ): Result<PackageManifest> {
        if (bytes == null) {
            return Result.Failure(
                PlatformError(
                    code =
                        ErrorCode.PACKAGE_STRUCTURE_INVALID,
                    message =
                        "Required file manifest.json is missing."
                )
            )
        }

        return try {
            val dto =
                json.decodeFromString<PackageManifestJsonDto>(
                    bytes.decodeToString()
                )

            dto.toDomain()
        } catch (exception: SerializationException) {
            Result.Failure(
                PlatformError(
                    code =
                        ErrorCode.PACKAGE_PARSE_FAILED,
                    message =
                        "Unable to parse manifest.json: " +
                                (
                                        exception.message
                                            ?: "Unknown error"
                                        ),
                    cause = exception
                )
            )
        } catch (exception: Exception) {
            Result.Failure(
                PlatformError(
                    code =
                        ErrorCode.PACKAGE_READ_FAILED,
                    message =
                        "Unable to load manifest.json: " +
                                (
                                        exception.message
                                            ?: "Unknown error"
                                        ),
                    cause = exception
                )
            )
        }
    }

    private inline fun <reified D, T> parseCollection(
        bytes: ByteArray?,
        path: String,
        mapper: (D) -> Result<T>
    ): Result<List<T>> {

        /*
         * Collection ط§ظ„ط؛ط§ط¦ط¨ط© ظ„ط§ طھط¹طھط¨ط± parse failure.
         *
         * ProfileValidator ظ‡ظˆ ط§ظ„ظ…ط³ط¤ظˆظ„ ط¹ظ† طھظ‚ط±ظٹط±
         * ظ‡ظ„ ط؛ظٹط§ط¨ظ‡ط§ ظ‚ط§ظ†ظˆظ†ظٹ ظ„ظ„ظ€ Profile ط£ظ… ظ„ط§.
         */
        if (bytes == null) {
            return Result.Success(
                emptyList()
            )
        }

        return try {
            val collection =
                json.decodeFromString<
                        PackageCollectionJsonDto<D>
                        >(
                    bytes.decodeToString()
                )

            val mappedItems =
                mutableListOf<T>()

            collection.items.forEach { dto ->
                when (
                    val mapped =
                        mapper(dto)
                ) {
                    is Result.Success ->
                        mappedItems.add(
                            mapped.data
                        )

                    is Result.Failure ->
                        return mapped
                }
            }

            Result.Success(
                mappedItems
            )
        } catch (exception: SerializationException) {
            Result.Failure(
                PlatformError(
                    code =
                        ErrorCode.PACKAGE_PARSE_FAILED,
                    message =
                        "Unable to parse $path: " +
                                (
                                        exception.message
                                            ?: "Unknown error"
                                        ),
                    cause = exception
                )
            )
        } catch (exception: Exception) {
            Result.Failure(
                PlatformError(
                    code =
                        ErrorCode.PACKAGE_READ_FAILED,
                    message =
                        "Unable to load $path: " +
                                (
                                        exception.message
                                            ?: "Unknown error"
                                        ),
                    cause = exception
                )
            )
        }
    }

    private suspend fun readCanonicalFiles(): PackageFileSet {
        return PackageFileSet(
            manifest =
                source.readBytesOrNull(
                    PackagePaths.MANIFEST
                ),
            entryPoints =
                source.readBytesOrNull(
                    PackagePaths.ENTRY_POINTS
                ),
            occasions =
                source.readBytesOrNull(
                    PackagePaths.OCCASIONS
                ),
            prayers =
                source.readBytesOrNull(
                    PackagePaths.PRAYERS
                ),
            prayerSequences =
                source.readBytesOrNull(
                    PackagePaths.PRAYER_SEQUENCES
                ),
            liturgicalItems =
                source.readBytesOrNull(
                    PackagePaths.LITURGICAL_ITEMS
                ),
            texts =
                source.readBytesOrNull(
                    PackagePaths.TEXTS
                ),
            petgomos =
                source.readBytesOrNull(
                    PackagePaths.PETGOMOS
                ),
            qolos =
                source.readBytesOrNull(
                    PackagePaths.QOLOS
                ),
            melodies =
                source.readBytesOrNull(
                    PackagePaths.MELODIES
                ),
            mediaAssets =
                source.readBytesOrNull(
                    PackagePaths.MEDIA_ASSETS
                ),

            qintos =
                source.readBytesOrNull(
                    PackagePaths.QINTOS
                ),
            melodyQintoAssignments =
                source.readBytesOrNull(
                    PackagePaths.MELODY_QINTO_ASSIGNMENTS
                )
        )
    }

    /**
     * ظٹط­طھظپط¸ ط¨ط§ظ„ظ€ bytes ط§ظ„ظ…ظ‚ط±ظˆط،ط© ط­طھظ‰ ظ„ط§ ظٹظ‚ط±ط£ load()
     * ط£ظٹ canonical file ط£ظƒط«ط± ظ…ظ† ظ…ط±ط©.
     */
    private data class PackageFileSet(
        val manifest: ByteArray?,
        val entryPoints: ByteArray?,
        val occasions: ByteArray?,
        val prayers: ByteArray?,
        val prayerSequences: ByteArray?,
        val liturgicalItems: ByteArray?,
        val texts: ByteArray?,
        val petgomos: ByteArray?,
        val qolos: ByteArray?,
        val melodies: ByteArray?,
        val mediaAssets: ByteArray?,
        val qintos: ByteArray?,
        val melodyQintoAssignments: ByteArray?
    )
}
