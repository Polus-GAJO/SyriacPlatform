package org.syriacplatform.user.storage

import kotlinx.coroutines.flow.StateFlow
import org.syriacplatform.user.models.UserPreferences

/**
 * Storage boundary for preferences. Platform-specific persistence can implement
 * this contract without changing the content repository or package loader.
 */
interface UserPreferencesStore {
    val preferences: StateFlow<UserPreferences>

    suspend fun save(preferences: UserPreferences)
}
