package org.syriacplatform.content.repository

import org.syriacplatform.common.result.Result
import org.syriacplatform.common.types.OccasionId
import org.syriacplatform.common.types.QoloId
import org.syriacplatform.content.models.EntryPoint
import org.syriacplatform.content.models.Occasion
import org.syriacplatform.content.models.Qolo
import org.syriacplatform.content.runtime.RuntimeEntryPoint
import org.syriacplatform.content.runtime.RuntimeOccasion
import org.syriacplatform.common.types.LiturgicalItemId
import org.syriacplatform.content.runtime.ResolvedLiturgicalItem

interface ContentRepository {

    suspend fun loadQolo(
        id: QoloId
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
        id: OccasionId
    ): Result<RuntimeOccasion>

    suspend fun loadLiturgicalItem(
        id: LiturgicalItemId
    ): Result<ResolvedLiturgicalItem>
}