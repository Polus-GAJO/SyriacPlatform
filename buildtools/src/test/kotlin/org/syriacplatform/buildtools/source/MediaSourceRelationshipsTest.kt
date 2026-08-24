package org.syriacplatform.buildtools.source

import java.nio.file.Files
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertEquals

class MediaSourceRelationshipsTest {

    @Test
    fun oneMediaAssetCanServeMultipleMelodies() {
        val directory =
            Files.createTempDirectory(
                "syriacplatform-media-many-to-many-"
            )

        try {
            directory
                .resolve("MediaAsset.csv")
                .writeText(
                    """
                    "MediaAssetID","MediaType","SourceRelativePath"
                    "7","AUDIO","audio/melodies/media-000007.mp3"
                    """.trimIndent() + "\n"
                )

            directory
                .resolve("MelodyMedia.csv")
                .writeText(
                    """
                    "MelodyMediaID","MelodyN","MediaAssetID","Role","Sort"
                    "1","424","7","RECORDING","1"
                    "2","2030","7","RECORDING","1"
                    """.trimIndent() + "\n"
                )

            val source =
                MediaSourceDataLoader()
                    .load(directory)

            assertEquals(
                1,
                source.mediaAssets.size
            )

            assertEquals(
                listOf(
                    424L,
                    2030L
                ),
                source.melodyMedia
                    .filter {
                        it.mediaAssetId == 7L
                    }
                    .map {
                        it.melodyId
                    }
            )
        } finally {
            directory
                .toFile()
                .deleteRecursively()
        }
    }
}