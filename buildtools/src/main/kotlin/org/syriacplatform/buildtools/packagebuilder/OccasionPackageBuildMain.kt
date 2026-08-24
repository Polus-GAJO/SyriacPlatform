package org.syriacplatform.buildtools.packagebuilder

import java.nio.file.Files
import java.nio.file.Path

internal data class OccasionPackageBuildArguments(
    val occasionId: Long,
    val sourceDirectory: Path,
    val mediaSourceDirectory: Path?,
    val mediaLibraryRoot: Path?,
    val outputDirectory: Path
) {
    val isMediaAware: Boolean
        get() =
            mediaSourceDirectory != null &&
                    mediaLibraryRoot != null
}

internal fun parseOccasionPackageBuildArguments(
    args: Array<String>
): OccasionPackageBuildArguments {

    require(
        args.size == 3 ||
                args.size == 5
    ) {
        "Expected either:" +
                "\n  <occasionId> <sourceDirectory> <outputDirectory>" +
                "\nor:" +
                "\n  <occasionId> <sourceDirectory> " +
                "<mediaSourceDirectory> <mediaLibraryRoot> " +
                "<outputDirectory>"
    }

    val occasionId =
        args[0].toLongOrNull()
            ?: error(
                "Occasion id must be a Long, " +
                        "but was '${args[0]}'."
            )

    require(
        occasionId > 0L
    ) {
        "Occasion id must be positive."
    }

    val sourceDirectory =
        normalizedPath(
            args[1]
        )

    require(
        Files.isDirectory(
            sourceDirectory
        )
    ) {
        "Author Database export for Occasion " +
                "$occasionId does not exist: " +
                sourceDirectory
    }

    return if (args.size == 3) {
        OccasionPackageBuildArguments(
            occasionId = occasionId,
            sourceDirectory =
                sourceDirectory,
            mediaSourceDirectory = null,
            mediaLibraryRoot = null,
            outputDirectory =
                normalizedPath(
                    args[2]
                )
        )
    } else {
        val mediaSourceDirectory =
            normalizedPath(
                args[2]
            )

        val mediaLibraryRoot =
            normalizedPath(
                args[3]
            )

        require(
            Files.isDirectory(
                mediaSourceDirectory
            )
        ) {
            "Author Database media export does not exist: " +
                    mediaSourceDirectory
        }

        require(
            Files.isDirectory(
                mediaLibraryRoot
            )
        ) {
            "Media library root does not exist: " +
                    mediaLibraryRoot
        }

        OccasionPackageBuildArguments(
            occasionId = occasionId,
            sourceDirectory =
                sourceDirectory,
            mediaSourceDirectory =
                mediaSourceDirectory,
            mediaLibraryRoot =
                mediaLibraryRoot,
            outputDirectory =
                normalizedPath(
                    args[4]
                )
        )
    }
}

fun main(args: Array<String>) {

    val buildArguments =
        parseOccasionPackageBuildArguments(
            args
        )

    val builder =
        OccasionPackageBuilder()

    val result =
        if (
            buildArguments.isMediaAware
        ) {
            builder.build(
                sourceDirectory =
                    buildArguments.sourceDirectory,
                mediaSourceDirectory =
                    requireNotNull(
                        buildArguments.mediaSourceDirectory
                    ),
                mediaLibraryRoot =
                    requireNotNull(
                        buildArguments.mediaLibraryRoot
                    ),
                occasionId =
                    buildArguments.occasionId,
                outputDirectory =
                    buildArguments.outputDirectory
            )
        } else {
            builder.build(
                sourceDirectory =
                    buildArguments.sourceDirectory,
                occasionId =
                    buildArguments.occasionId,
                outputDirectory =
                    buildArguments.outputDirectory
            )
        }

    println(
        "Occasion ${result.occasionId} package built successfully."
    )

    println(
        "Mode: " +
                if (buildArguments.isMediaAware) {
                    "media-aware"
                } else {
                    "legacy-no-media"
                }
    )

    println(
        "Source: ${buildArguments.sourceDirectory}"
    )

    if (
        buildArguments.isMediaAware
    ) {
        println(
            "Media export: " +
                    buildArguments.mediaSourceDirectory
        )

        println(
            "Media library: " +
                    buildArguments.mediaLibraryRoot
        )

        println(
            "Packaged media assets: " +
                    result.packageData.mediaAssets.size
        )
    }

    println(
        "Output: ${result.outputDirectory}"
    )

    println(
        "Prayers: ${result.prayerCount}"
    )

    println(
        "Liturgical items: ${result.liturgicalItemCount}"
    )
}

private fun normalizedPath(
    rawPath: String
): Path {
    return Path.of(
        rawPath
    )
        .toAbsolutePath()
        .normalize()
}