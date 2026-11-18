package org.syriacplatform.presentation.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

/**
 * Central design references for liturgical content.
 *
 * The 26/22/25 sizes are the approved starting proportions for Serto Jerusalem,
 * Amiri and Noto Sans respectively. They are not automatically
 * applied to existing screens; migrate each usage after visual verification.
 */
object LiturgicalColors {
    val Black = Color(0xFF000000)
    val Red = Color(0xFFE00000)
}

enum class LiturgicalLanguage {
    Syriac,
    Arabic,
    English,
    French
}

enum class LiturgicalTextRole {
    Body,
    Note,
    Title,
    Section,
    Secondary,
    Caption
}

object LiturgicalTypography {
    const val SyriacBaseSp = 26f
    const val ArabicBaseSp = 22f
    const val LatinBaseSp = 25f
    const val LandscapeContentMultiplier = 1.05f

    fun baseSizeSp(language: LiturgicalLanguage): Float = when (language) {
        LiturgicalLanguage.Syriac -> SyriacBaseSp
        LiturgicalLanguage.Arabic -> ArabicBaseSp
        LiturgicalLanguage.English, LiturgicalLanguage.French -> LatinBaseSp
    }

    /**
     * Roles are intentionally not assigned speculative size multipliers yet.
     * Only body and note content receive the proposed 5% landscape adjustment.
     */
    fun size(
        language: LiturgicalLanguage,
        role: LiturgicalTextRole,
        userScale: Float = 1f,
        isLandscape: Boolean = false
    ): TextUnit {
        require(userScale.isFinite() && userScale > 0f) {
            "User font scale must be finite and positive"
        }
        val orientationScale =
            if (isLandscape && (role == LiturgicalTextRole.Body ||
                    role == LiturgicalTextRole.Note)) {
                LandscapeContentMultiplier
            } else {
                1f
            }
        return (baseSizeSp(language) * userScale * orientationScale).sp
    }
}
