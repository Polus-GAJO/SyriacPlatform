package org.syriacplatform.user.storage

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.syriacplatform.user.models.UserPreferences

/**
 * Device-local preferences, independent of the published content packages.
 * Uses application context so it never retains an Activity across rotation.
 */
class AndroidUserPreferencesStore(
    context: Context,
    preferencesFileName: String = "syriacplatform.shhimo.user.preferences.v1"
) : UserPreferencesStore {
    private val storage = context.applicationContext.getSharedPreferences(
        preferencesFileName,
        Context.MODE_PRIVATE
    )

    private val state = MutableStateFlow(readStoredPreferences())

    override val preferences: StateFlow<UserPreferences> = state

    override suspend fun save(preferences: UserPreferences) {
        val committed = storage.edit()
            .apply {
                if (preferences.interfaceLanguage == null) remove(KEY_INTERFACE_LANGUAGE)
                else putString(KEY_INTERFACE_LANGUAGE, preferences.interfaceLanguage)
                if (preferences.translationLanguage == null) remove(KEY_TRANSLATION_LANGUAGE)
                else putString(KEY_TRANSLATION_LANGUAGE, preferences.translationLanguage)
                putFloat(KEY_FONT_SCALE, preferences.fontScale)
            }
            .commit()

        if (!committed) {
            throw IllegalStateException("Failed to persist user preferences")
        }
        state.value = preferences
    }

    private fun readStoredPreferences(): UserPreferences {
        val scale = storage.getFloat(KEY_FONT_SCALE, 1.0f)
        return UserPreferences(
            interfaceLanguage = storage.getString(KEY_INTERFACE_LANGUAGE, null),
            translationLanguage = storage.getString(KEY_TRANSLATION_LANGUAGE, null),
            fontScale = scale.takeIf { it.isFinite() && it > 0f } ?: 1.0f
        )
    }

    private companion object {
        const val KEY_INTERFACE_LANGUAGE = "interface_language"
        const val KEY_TRANSLATION_LANGUAGE = "translation_language"
        const val KEY_FONT_SCALE = "font_scale"
    }
}
