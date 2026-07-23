package org.syriacplatform.content.contracts

import org.syriacplatform.common.result.Result
import org.syriacplatform.common.types.QoloId
import org.syriacplatform.content.models.Qolo
import org.syriacplatform.kernel.PlatformService

/**
 * العقد الأساسي للوصول إلى المحتوى الليتورجي.
 */
interface ContentService : PlatformService {

    suspend fun loadQolo(
        qoloId: QoloId
    ): Result<Qolo>

    suspend fun loadAllQolos(): Result<List<Qolo>>
}