package org.syriacplatform.content.services
import org.syriacplatform.common.types.ErrorCode
import org.syriacplatform.common.types.PlatformError
import org.syriacplatform.common.result.Result
import org.syriacplatform.common.types.QoloId
import org.syriacplatform.common.types.RuntimeState
import org.syriacplatform.content.contracts.ContentService
import org.syriacplatform.content.models.Qolo
import org.syriacplatform.kernel.ServiceMetadata

/**
 * التنفيذ الافتراضي لخدمة المحتوى.
 */
class DefaultContentService : ContentService {
    private val qolos = listOf(
        Qolo(
            id = QoloId(1),
            name = "ܩܳܠܳܐ ܢܽܘܗܪܳܢܳܐ",
            poeticMeter = "12"
        )
    )
    override val metadata = ServiceMetadata(
        name = "Content Service",
        version = "1.0"
    )

    override var runtimeState = RuntimeState.NotInitialized
        private set

    override fun initialize() {
        runtimeState = RuntimeState.Ready
    }

    override fun loadQolo(
        qoloId: QoloId
    ): Result<Qolo> {
        val qolo = qolos.firstOrNull { it.id == qoloId }

        return if (qolo != null) {
            Result.Success(qolo)
        } else {
            Result.Failure(
                PlatformError(
                    code = ErrorCode.CONTENT_NOT_FOUND,
                    message = "Qolo was not found: ${qoloId.value}"
                )
            )
        }
    }
}