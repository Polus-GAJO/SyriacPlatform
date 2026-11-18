package org.syriacplatform.user.storage

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.syriacplatform.user.models.UserPreferences

/**
 * Ephemeral implementation for tests and previews.
 * It does not persist across application restarts.
 */
class InMemoryUserPreferencesStore(
    initialPreferences: UserPreferences = UserPreferences()
) : UserPreferencesStore {
    private val state = MutableStateFlow(initialPreferences)

    override val preferences: StateFlow<UserPreferences> = state

    override suspend fun save(preferences: UserPreferences) {
        state.value = preferences
    }
}
