package org.syriacplatform.content.repository

import kotlinx.serialization.json.Json
import org.syriacplatform.resources.Res
import org.syriacplatform.common.result.Result
import org.syriacplatform.common.types.ErrorCode
import org.syriacplatform.common.types.PlatformError
import org.syriacplatform.common.types.QoloId
import org.syriacplatform.content.dto.QoloJsonDto
import org.syriacplatform.content.models.Qolo

class JsonContentRepository(
    private val json: Json = Json {
        ignoreUnknownKeys = true
    }
) : ContentRepository {

    private var cachedQolos: List<Qolo>? = null

    override suspend fun loadQolo(
        id: QoloId
    ): Result<Qolo> {
        return when (val result = loadAllQolos()) {
            is Result.Success -> {
                val qolo = result.data.firstOrNull { it.id == id }

                if (qolo != null) {
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

            is Result.Failure -> result
        }
    }

    override suspend fun loadAllQolos(): Result<List<Qolo>> {
        cachedQolos?.let { qolos ->
            return Result.Success(qolos)
        }

        return try {
            val bytes = Res.readBytes("files/content/qolos.json")
            val text = bytes.decodeToString()

            val qolos = json
                .decodeFromString<List<QoloJsonDto>>(text)
                .map(QoloJsonDto::toDomain)

            cachedQolos = qolos

            Result.Success(qolos)
        } catch (exception: Exception) {
            Result.Failure(
                PlatformError(
                    code = ErrorCode.CONTENT_NOT_FOUND,
                    message = "Unable to load qolos.json: ${exception.message}"
                )
            )
        }
    }
}