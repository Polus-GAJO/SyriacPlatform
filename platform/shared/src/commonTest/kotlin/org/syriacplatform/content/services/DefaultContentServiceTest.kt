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
import org.syriacplatform.content.models.EntryPoint
import org.syriacplatform.content.models.Occasion

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

    @Test
    fun loadEntryPointsReturnsAvailableEntryPoints() =
        runTest {
            val result =
                service.loadEntryPoints()

            val success =
                assertIs<
                        Result.Success<List<EntryPoint>>
                        >(
                    result
                )

            assertEquals(
                1,
                success.data.size
            )

            assertEquals(
                EntryPointId(1),
                success.data.single().id
            )
        }

    @Test
    fun loadOccasionsReturnsAvailableOccasions() =
        runTest {
            val result =
                service.loadOccasions()

            val success =
                assertIs<
                        Result.Success<List<Occasion>>
                        >(
                    result
                )

            assertEquals(
                1,
                success.data.size
            )

            assertEquals(
                OccasionId(1),
                success.data.single().id
            )
        }
}