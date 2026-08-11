package org.syriacplatform.packageformat.loading

import org.jetbrains.compose.resources.MissingResourceException
import org.syriacplatform.resources.Res

/**
 * PackageSource يقرأ الملفات من Compose Multiplatform Resources.
 *
 * PackageSource يتعامل مع المسارات القانونية للحزمة مثل:
 *
 * manifest.json
 * content/qolos.json
 *
 * بينما Compose Resources يحتاج prefix داخلي:
 *
 * files/
 */
class ResourcePackageSource :
    PackageSource {

    override suspend fun readBytesOrNull(
        path: String
    ): ByteArray? {
        return try {
            Res.readBytes(
                "files/$path"
            )
        } catch (_: MissingResourceException) {
            null
        }
    }
}