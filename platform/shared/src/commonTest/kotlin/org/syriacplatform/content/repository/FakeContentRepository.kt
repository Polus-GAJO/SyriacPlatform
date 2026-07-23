package org.syriacplatform.content.repository

import org.syriacplatform.common.result.Result
import org.syriacplatform.common.types.ErrorCode
import org.syriacplatform.common.types.PlatformError
import org.syriacplatform.common.types.QoloId
import org.syriacplatform.content.models.Qolo

class FakeContentRepository(
    private val qolos: List<Qolo> = listOf(
        Qolo(
            id = QoloId(1),
            name = "ܩܳܠܳܐ ܢܽܘܗܪܳܢܳܐ",
            poeticMeter = "12"
        )
    )
) : ContentRepository {

    override suspend fun loadQolo(
        id: QoloId
    ): Result<Qolo> {
        val qolo = qolos.firstOrNull {
            it.id == id
        }

        return if (qolo != null) {
            Result.Success(qolo)
        } else {
            Result.Failure(
                PlatformError(
                    code = ErrorCode.CONTENT_NOT_FOUND,
                    message = "Qolo was not found: ${id.value}"
                )
            )
        }
    }

    override suspend fun loadAllQolos(): Result<List<Qolo>> {
        return Result.Success(qolos)
    }
}