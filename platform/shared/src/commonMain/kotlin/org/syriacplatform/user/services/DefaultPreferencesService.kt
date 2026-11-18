package org.syriacplatform.user.services

import kotlinx.coroutines.flow.StateFlow
import org.syriacplatform.common.types.RuntimeState
import org.syriacplatform.kernel.ServiceMetadata
import org.syriacplatform.user.contracts.PreferencesService
import org.syriacplatform.user.models.UserPreferences
import org.syriacplatform.user.storage.UserPreferencesStore

class DefaultPreferencesService(
    private val store: UserPreferencesStore
) : PreferencesService {
    override val metadata = ServiceMetadata(
        name = "Preferences Service",
        version = "1.0"
    )

    override var runtimeState = RuntimeState.NotInitialized
        private set

    override val preferences: StateFlow<UserPreferences>
        get() = store.preferences

    override fun initialize() {
        runtimeState = RuntimeState.Ready
    }

    override suspend fun update(preferences: UserPreferences) {
        store.save(preferences)
    }

    override fun shutdown() {
        runtimeState = RuntimeState.NotInitialized
    }
}
