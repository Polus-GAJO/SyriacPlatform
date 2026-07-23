package org.syriacplatform.content.services

import org.syriacplatform.common.result.Result
import org.syriacplatform.common.types.QoloId
import org.syriacplatform.common.types.RuntimeState
import org.syriacplatform.content.contracts.ContentService
import org.syriacplatform.content.models.Qolo
import org.syriacplatform.content.repository.ContentRepository
import org.syriacplatform.content.repository.JsonContentRepository
import org.syriacplatform.kernel.ServiceMetadata

/**
 * التنفيذ الافتراضي لخدمة المحتوى.
 */
class DefaultContentService(
    private val repository: ContentRepository =
        JsonContentRepository()
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
}