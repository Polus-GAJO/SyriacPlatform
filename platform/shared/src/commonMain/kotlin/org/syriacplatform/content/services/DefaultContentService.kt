package org.syriacplatform.content.services

import org.syriacplatform.common.result.Result
import org.syriacplatform.common.types.QoloId
import org.syriacplatform.common.types.RuntimeState
import org.syriacplatform.content.contracts.ContentService
import org.syriacplatform.content.models.Qolo
import org.syriacplatform.content.repository.ContentRepository
import org.syriacplatform.kernel.ServiceMetadata
import org.syriacplatform.common.types.OccasionId
import org.syriacplatform.content.runtime.RuntimeEntryPoint
import org.syriacplatform.content.runtime.RuntimeOccasion
import org.syriacplatform.content.models.EntryPoint
import org.syriacplatform.content.models.Occasion

/**
 * التنفيذ الافتراضي لخدمة المحتوى.
 */
class DefaultContentService(
    private val repository: ContentRepository
) : ContentService {

    override val metadata = ServiceMetadata(
        name = "Content Service",
        version = "1.0"
    )

    override var runtimeState =
        RuntimeState.NotInitialized
        private set

    override fun initialize() {
        runtimeState = RuntimeState.Ready
    }

    override suspend fun loadQolo(
        qoloId: QoloId
    ): Result<Qolo> {
        return repository.loadQolo(qoloId)
    }

    override suspend fun loadAllQolos(): Result<List<Qolo>> {
        return repository.loadAllQolos()
    }

    override suspend fun loadDefaultEntryPoint():
            Result<RuntimeEntryPoint> {
        return repository.loadDefaultEntryPoint()
    }

    override suspend fun loadOccasion(
        occasionId: OccasionId
    ): Result<RuntimeOccasion> {
        return repository.loadOccasion(
            occasionId
        )
    }

    override suspend fun loadEntryPoints():
            Result<List<EntryPoint>> {
        return repository.loadEntryPoints()
    }

    override suspend fun loadOccasions():
            Result<List<Occasion>> {
        return repository.loadOccasions()
    }
}