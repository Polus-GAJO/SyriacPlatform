package org.syriacplatform.content.repository

import kotlinx.serialization.json.Json
import org.syriacplatform.common.result.Result
import org.syriacplatform.common.types.ErrorCode
import org.syriacplatform.common.types.PlatformError
import org.syriacplatform.common.types.QoloId
import org.syriacplatform.content.models.Qolo
import org.syriacplatform.packageformat.dto.PackageCollectionJsonDto
import org.syriacplatform.packageformat.dto.QoloJsonDto
import org.syriacplatform.packageformat.mappers.toDomain
import org.syriacplatform.resources.Res

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
                val qolo = result.data.firstOrNull { item ->
                    item.id == id
                }

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
            val bytes = Res.readBytes(
                "files/content/qolos.json"
            )

            val text = bytes.decodeToString()

            val collection = json.decodeFromString<
                    PackageCollectionJsonDto<QoloJsonDto>
                    >(text)

            val qolos = mutableListOf<Qolo>()

            for (dto in collection.items) {
                when (val mappingResult = dto.toDomain()) {
                    is Result.Success -> {
                        qolos += mappingResult.data
                    }

                    is Result.Failure -> {
                        return mappingResult
                    }
                }
            }

            val immutableQolos = qolos.toList()

            cachedQolos = immutableQolos

            Result.Success(immutableQolos)
        } catch (exception: Exception) {
            Result.Failure(
                PlatformError(
                    code = classifyLoadingError(exception),
                    message =
                        "Unable to load qolos.json: " +
                                (exception.message ?: "Unknown error"),
                    cause = exception
                )
            )
        }
    }

    private fun classifyLoadingError(
        exception: Exception
    ): ErrorCode {
        return when (exception) {
            is kotlinx.serialization.SerializationException ->
                ErrorCode.PACKAGE_PARSE_FAILED

            else ->
                ErrorCode.PACKAGE_READ_FAILED
        }
    }
}