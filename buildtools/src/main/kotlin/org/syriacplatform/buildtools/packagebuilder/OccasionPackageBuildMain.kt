package org.syriacplatform.buildtools.packagebuilder

import java.nio.file.Files
import java.nio.file.Path

fun main(args: Array<String>) {

    require(args.size == 3) {
        "Expected arguments: <occasionId> <sourceDirectory> <outputDirectory>"
    }

    val occasionId =
        args[0].toLongOrNull()
            ?: error(
                "Occasion id must be a Long, but was '${args[0]}'."
            )

    require(occasionId > 0L) {
        "Occasion id must be positive."
    }

    val sourceDirectory =
        Path.of(args[1])
            .toAbsolutePath()
            .normalize()

    require(
        Files.isDirectory(
            sourceDirectory
        )
    ) {
        "Author Database export for Occasion " +
                "$occasionId does not exist: " +
                sourceDirectory
    }

    val outputDirectory =
        Path.of(args[2])
            .toAbsolutePath()
            .normalize()

    val result =
        OccasionPackageBuilder()
            .build(
                sourceDirectory = sourceDirectory,
                occasionId = occasionId,
                outputDirectory = outputDirectory
            )

    println(
        "Occasion ${result.occasionId} package built successfully."
    )

    println(
        "Source: $sourceDirectory"
    )

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