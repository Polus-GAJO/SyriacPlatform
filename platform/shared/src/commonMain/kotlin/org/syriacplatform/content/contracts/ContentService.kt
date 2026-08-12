package org.syriacplatform.content.contracts

import org.syriacplatform.common.result.Result
import org.syriacplatform.common.types.OccasionId
import org.syriacplatform.common.types.QoloId
import org.syriacplatform.content.models.EntryPoint
import org.syriacplatform.content.models.Occasion
import org.syriacplatform.content.models.Qolo
import org.syriacplatform.content.runtime.RuntimeEntryPoint
import org.syriacplatform.content.runtime.RuntimeOccasion
import org.syriacplatform.kernel.PlatformService

/**
 * العقد الأساسي للوصول إلى المحتوى الليتورجي.
 */
interface ContentService : PlatformService {

    suspend fun loadQolo(
        qoloId: QoloId
    ): Result<Qolo>

    suspend fun loadAllQolos():
            Result<List<Qolo>>

    suspend fun loadEntryPoints():
            Result<List<EntryPoint>>

    suspend fun loadOccasions():
            Result<List<Occasion>>

    suspend fun loadDefaultEntryPoint():
            Result<RuntimeEntryPoint>

    suspend fun loadOccasion(
        occasionId: OccasionId
    ): Result<RuntimeOccasion>
}