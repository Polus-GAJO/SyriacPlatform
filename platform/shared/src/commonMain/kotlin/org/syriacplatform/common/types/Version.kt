package org.syriacplatform.common.types

/**
 * يمثل إصدارًا دلاليًا بسيطًا داخل المنصة
 * بالصيغة:
 *
 * major.minor.patch
 *
 * مثال:
 * 1.2.0
 */
data class Version(
    val major: Int,
    val minor: Int,
    val patch: Int
) : Comparable<Version> {

    init {
        require(major >= 0) {
            "Version major must not be negative."
        }

        require(minor >= 0) {
            "Version minor must not be negative."
        }

        require(patch >= 0) {
            "Version patch must not be negative."
        }
    }

    override fun compareTo(
        other: Version
    ): Int {
        val majorComparison =
            major.compareTo(other.major)

        if (majorComparison != 0) {
            return majorComparison
        }

        val minorComparison =
            minor.compareTo(other.minor)

        if (minorComparison != 0) {
            return minorComparison
        }

        return patch.compareTo(other.patch)
    }

    override fun toString(): String {
        return "$major.$minor.$patch"
    }

    companion object {

        /**
         * يحاول تحويل نص بالشكل major.minor.patch
         * إلى Version.
         *
         * يعيد null إذا كانت الصيغة غير قانونية.
         */
        fun parseOrNull(
            value: String
        ): Version? {
            val parts =
                value
                    .trim()
                    .split(".")

            if (parts.size != 3) {
                return null
            }

            val major =
                parts[0].toIntOrNull()
                    ?: return null

            val minor =
                parts[1].toIntOrNull()
                    ?: return null

            val patch =
                parts[2].toIntOrNull()
                    ?: return null

            if (
                major < 0 ||
                minor < 0 ||
                patch < 0
            ) {
                return null
            }

            return Version(
                major = major,
                minor = minor,
                patch = patch
            )
        }
    }
}