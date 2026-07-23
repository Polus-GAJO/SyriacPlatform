package org.syriacplatform.content.repository

import org.syriacplatform.common.result.Result
import org.syriacplatform.common.types.QoloId
import org.syriacplatform.content.models.Qolo

interface ContentRepository {

    suspend fun loadQolo(
        id: QoloId
    ): Result<Qolo>

    suspend fun loadAllQolos(): Result<List<Qolo>>
}