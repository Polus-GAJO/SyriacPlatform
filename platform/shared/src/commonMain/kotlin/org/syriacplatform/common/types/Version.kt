package org.syriacplatform.common.types

/**
 * يمثل إصدار أي مكون داخل المنصة.
 */
data class Version(
    val major: Int,
    val minor: Int,
    val patch: Int
) {

    override fun toString(): String {
        return "$major.$minor.$patch"
    }
}