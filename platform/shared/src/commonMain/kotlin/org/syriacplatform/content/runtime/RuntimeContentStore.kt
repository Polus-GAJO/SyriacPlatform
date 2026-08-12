package org.syriacplatform.content.runtime

import org.syriacplatform.packageformat.parsed.ParsedApplicationPackage

/**
 * المحتوى الجاهز للاستخدام داخل Runtime مع فهارسه.
 *
 * لا يمكن إنشاء RuntimeContentStore إلا من محتوى
 * مرّ مسبقًا عبر مسار Package Loading + Validation.
 *
 * مسؤولية التأكد من نجاح Validation تقع على الطبقة
 * التي تستدعي from(packageData).
 */
data class RuntimeContentStore(
    val content: RuntimeContent,
    val index: RuntimeContentIndex
) {

    companion object {

        fun from(
            packageData: ParsedApplicationPackage
        ): RuntimeContentStore {
            val content =
                RuntimeContent.from(
                    packageData
                )

            return RuntimeContentStore(
                content = content,
                index =
                    RuntimeContentIndex.from(
                        content
                    )
            )
        }
    }
}