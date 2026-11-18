package org.syriacplatform.user.models

/**
 * User-owned reading preferences, independent of published liturgical packages.
 * UI language and preferred translation language are intentionally separate.
 */
data class UserPreferences(
    val interfaceLanguage: String? = null,
    val translationLanguage: String? = null,
    val fontScale: Float = 1.0f
) {
    init {
        require(fontScale.isFinite() && fontScale > 0f) {
            "Font scale must be finite and positive"
        }
    }
}
