package org.syriacplatform.packageformat.loading

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertNull

class PackageSourceTest {

    private class FakePackageSource(
        private val files: Map<String, ByteArray>
    ) : PackageSource {

        override suspend fun readBytesOrNull(
            path: String
        ): ByteArray? {
            return files[path]
        }
    }

    @Test
    fun existingFileReturnsBytes() =
        kotlinx.coroutines.test.runTest {
            val expected =
                """{"items":[]}"""
                    .encodeToByteArray()

            val source =
                FakePackageSource(
                    files = mapOf(
                        "content/texts.json" to expected
                    )
                )

            val actual =
                source.readBytesOrNull(
                    "content/texts.json"
                )

            assertContentEquals(
                expected,
                actual
            )
        }

    @Test
    fun missingFileReturnsNull() =
        kotlinx.coroutines.test.runTest {
            val source =
                FakePackageSource(
                    files = emptyMap()
                )

            val actual =
                source.readBytesOrNull(
                    "content/texts.json"
                )

            assertNull(actual)
        }
}