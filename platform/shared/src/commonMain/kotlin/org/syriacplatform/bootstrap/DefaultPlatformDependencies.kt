package org.syriacplatform.bootstrap

import org.syriacplatform.common.types.Version
import org.syriacplatform.content.repository.ApplicationPackageContentRepository
import org.syriacplatform.packageformat.loading.ApplicationPackageLoader
import org.syriacplatform.packageformat.loading.ResourcePackageSource
import org.syriacplatform.packagevalidation.compatibility.CoreCompatibility

/**
 * ينشئ الاعتمادات الافتراضية المستخدمة
 * عند تشغيل المنصة داخل التطبيق الحقيقي.
 *
 * المحتوى يُقرأ الآن من Application Package كاملة،
 * ثم يمر عبر parsing وPackage Validation قبل أن
 * يصبح متاحًا لخدمات Core.
 */
object DefaultPlatformDependencies {

    fun create(): PlatformDependencies {
        val coreCompatibility =
            CoreCompatibility(
                version = Version(1, 0, 0),
                supportedSchemaVersions =
                    setOf(
                        "1.0"
                    )
            )

        val packageLoader =
            ApplicationPackageLoader(
                source =
                    ResourcePackageSource()
            )

        val contentRepository =
            ApplicationPackageContentRepository(
                loader = packageLoader,
                coreCompatibility =
                    coreCompatibility
            )

        return PlatformDependencies(
            contentRepository =
                contentRepository
        )
    }
}