package org.syriacplatform.packageformat.loading

import java.nio.file.Files
import java.nio.file.Path
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import org.syriacplatform.packageformat.models.PackageProfile
import org.syriacplatform.packagevalidation.compatibility.CoreCompatibilityDefaults

class ShhimoDailyPackageIntegrationTest {
    @Test
    fun generatedShhimoPackageLoadsAndNavigationMatchesContent() = runTest {
        val root = Path.of(
            System.getProperty(
                "syriacplatform.test.shhimoPackageDir",
                "../../author-database/preview/shhimo-daily-v2"
            )
        ).toAbsolutePath().normalize()
        assertTrue(Files.isDirectory(root), "Shhimo package not found: $root")

        val source = object : PackageSource {
            override suspend fun readBytesOrNull(path: String): ByteArray? {
                val file = root.resolve(path).normalize()
                require(file.startsWith(root)) { "Package path escapes root: $path" }
                return if (Files.isRegularFile(file)) Files.readAllBytes(file) else null
            }
        }
        val result = ApplicationPackageLoader(source).load(
            CoreCompatibilityDefaults.CURRENT
        )
        if (result is PackageLoadResult.ValidationFailed) {
            error("Shhimo validation failed: " + result.validationReport.issues.joinToString {
                "${it.code}: ${it.message}"
            })
        }
        val loaded = assertIs<PackageLoadResult.Success>(result)
        assertEquals(PackageProfile.SHHIMA, loaded.packageData.manifest.profile)
        assertTrue(loaded.packageData.entryPoints.isEmpty())
        assertTrue(loaded.packageData.occasions.isEmpty())

        val navigation = ShhimoDailyNavigationLoader(source).load()
        assertEquals(1L, navigation.bookId)
        assertEquals(7, navigation.days.size)
        val navigationIds = navigation.days.flatMap { day ->
            day.prayers.map { it.sequenceId }
        }
        assertEquals(78, navigationIds.size)
        val contentIds = loaded.packageData.prayerSequences.map { it.id.value }
        assertEquals(contentIds.toSet(), navigationIds.toSet())
        assertEquals(431, loaded.packageData.liturgicalItems.size)
    }
}
