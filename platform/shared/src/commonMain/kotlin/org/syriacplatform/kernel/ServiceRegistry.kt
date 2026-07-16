package org.syriacplatform.kernel

import kotlin.reflect.KClass
import org.syriacplatform.common.result.Result
import org.syriacplatform.common.types.ErrorCode
import org.syriacplatform.common.types.PlatformError

/**
 * يسجل خدمات المنصة ويتيح استرجاعها حسب نوع العقد.
 */
class ServiceRegistry {

    private val services =
        mutableMapOf<KClass<out PlatformService>, PlatformService>()

    fun <T : PlatformService> register(
        serviceType: KClass<T>,
        service: T
    ): Result<T> {
        services[serviceType] = service
        return Result.Success(service)
    }

    fun <T : PlatformService> resolve(
        serviceType: KClass<T>
    ): Result<T> {
        val service = services[serviceType]
            ?: return Result.Failure(
                PlatformError(
                    code = ErrorCode.SERVICE_NOT_REGISTERED,
                    message = "Service is not registered: ${serviceType.simpleName}"
                )
            )

        @Suppress("UNCHECKED_CAST")
        return Result.Success(service as T)
    }

    fun getRegisteredServices(): List<ServiceMetadata> {
        return services.values.map { it.metadata }
    }
}