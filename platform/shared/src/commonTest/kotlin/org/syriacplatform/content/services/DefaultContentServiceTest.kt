package org.syriacplatform.content.services

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.coroutines.test.runTest
import org.syriacplatform.common.result.Result
import org.syriacplatform.common.types.EntryPointId
import org.syriacplatform.common.types.OccasionId
import org.syriacplatform.content.repository.FakeContentRepository
import org.syriacplatform.content.runtime.RuntimeEntryPoint
import org.syriacplatform.content.runtime.RuntimeOccasion

class DefaultContentServiceTest {

    private val service =
        DefaultContentService(
            repository = FakeContentRepository()
        )

    @Test
    fun loadDefaultEntryPointReturnsResolvedRuntimeEntryPoint() =
        runTest {
            val result =
                service.loadDefaultEntryPoint()

            val success =
                assertIs<
                        Result.Success<RuntimeEntryPoint>
                        >(
                    result
                )

            assertEquals(
                EntryPointId(1),
                success.data.entryPoint.id
            )

            assertEquals(
                OccasionId(1),
                success.data.occasion.occasion.id
            )
        }

    @Test
    fun loadOccasionReturnsResolvedRuntimeOccasion() =
        runTest {
            val result =
                service.loadOccasion(
                    OccasionId(1)
                )

            val success =
                assertIs<
                        Result.Success<RuntimeOccasion>
                        >(
                    result
                )

            assertEquals(
                OccasionId(1),
                success.data.occasion.id
            )

            assertEquals(
                "Test Occasion",
                success.data.occasion.name
            )
        }
}