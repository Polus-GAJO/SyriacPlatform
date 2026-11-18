package org.syriacplatform.user.storage

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import org.syriacplatform.user.models.UserPreferences

class InMemoryUserPreferencesStoreTest {
    @Test
    fun startsWithIndependentUnsetLanguagePreferences() {
        val store = InMemoryUserPreferencesStore()
        assertEquals(null, store.preferences.value.interfaceLanguage)
        assertEquals(null, store.preferences.value.translationLanguage)
        assertEquals(1.0f, store.preferences.value.fontScale)
    }

    @Test
    fun savesPreferencesAndPublishesLatestValue() = runTest {
        val store = InMemoryUserPreferencesStore()
        val updated = UserPreferences(
            interfaceLanguage = "en",
            translationLanguage = "ar",
            fontScale = 1.25f
        )
        store.save(updated)
        assertEquals(updated, store.preferences.value)
    }

    @Test
    fun rejectsInvalidFontScale() {
        assertFailsWith<IllegalArgumentException> {
            UserPreferences(fontScale = 0f)
        }
        assertFailsWith<IllegalArgumentException> {
            UserPreferences(fontScale = Float.NaN)
        }
    }

    @Test
    fun separateStoreInstancesDoNotShareState() = runTest {
        val first = InMemoryUserPreferencesStore()
        val second = InMemoryUserPreferencesStore()
        first.save(UserPreferences(translationLanguage = "fr"))
        assertEquals(null, second.preferences.value.translationLanguage)
    }
}
