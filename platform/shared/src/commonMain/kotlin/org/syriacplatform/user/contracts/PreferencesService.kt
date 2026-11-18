package org.syriacplatform.user.contracts

import kotlinx.coroutines.flow.StateFlow
import org.syriacplatform.kernel.PlatformService
import org.syriacplatform.user.models.UserPreferences

interface PreferencesService : PlatformService {
    val preferences: StateFlow<UserPreferences>

    suspend fun update(preferences: UserPreferences)
}
