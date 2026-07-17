package org.syriacplatform.kernel

import kotlin.reflect.KClass
import org.syriacplatform.common.result.Result

/**
 * النواة المركزية للمنصة.
 */
class PlatformKernel(
    private val registry: ServiceRegistry = ServiceRegistry()
) {

    fun <T : PlatformService> registerService(
        serviceType: KClass<T>,
        service: T
    ): Result<T> {
        return registry.register(
            serviceType = serviceType,
            service = service
        )
    }

    fun initialize() {
        registry.forEachService { service ->
            service.initialize()
        }
    }

    fun <T : PlatformService> resolveService(
        serviceType: KClass<T>
    ): Result<T> {
        return registry.resolve(serviceType)
    }

    fun getRegisteredServices(): List<ServiceMetadata> {
        return registry.getRegisteredServices()
    }


}