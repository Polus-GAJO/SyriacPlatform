package org.syriacplatform.user.storage

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.syriacplatform.user.models.UserPreferences

@RunWith(AndroidJUnit4::class)
class AndroidUserPreferencesStoreTest {
    private val context
        get() = InstrumentationRegistry.getInstrumentation().targetContext

    // Never touch the production preferences file during tests.
    private val testFile = "syriacplatform.user.preferences.instrumentation-test"

    @Before
    fun clearBefore() {
        clearTestPreferences()
    }

    @After
    fun clearAfter() {
        clearTestPreferences()
    }

    @Test
    fun preferencesSurviveCreatingANewStoreInstance() = runBlocking {
        val first = AndroidUserPreferencesStore(context, testFile)
        val expected = UserPreferences(
            interfaceLanguage = "en",
            translationLanguage = "ar",
            fontScale = 1.35f
        )
        first.save(expected)

        val reopened = AndroidUserPreferencesStore(context, testFile)
        assertEquals(expected, reopened.preferences.value)
    }

    @Test
    fun nullableLanguagesAreRemovedFromPersistentStorage() = runBlocking {
        val first = AndroidUserPreferencesStore(context, testFile)
        first.save(UserPreferences(interfaceLanguage = "fr", translationLanguage = "ar"))
        first.save(UserPreferences(fontScale = 1.1f))

        val reopened = AndroidUserPreferencesStore(context, testFile)
        assertNull(reopened.preferences.value.interfaceLanguage)
        assertNull(reopened.preferences.value.translationLanguage)
        assertEquals(1.1f, reopened.preferences.value.fontScale)
    }

    @Test
    fun invalidStoredFontScaleFallsBackToDefault() {
        context.getSharedPreferences(testFile, 0).edit()
            .putFloat("font_scale", Float.NaN)
            .commit()

        val reopened = AndroidUserPreferencesStore(context, testFile)
        assertEquals(1.0f, reopened.preferences.value.fontScale)
    }

    private fun clearTestPreferences() {
        check(context.getSharedPreferences(testFile, 0).edit().clear().commit())
    }
}
