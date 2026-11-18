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
        val initialized = mutableListOf<PlatformService>()
        registry.forEachService { service ->
            try {
                service.initialize()
                initialized.add(service)
            } catch (failure: Throwable) {
                // The failing service may have acquired resources before throwing.
                // Release it, then roll back already initialized services in reverse order.
                val toClose = listOf(service) + initialized.asReversed()
                toClose.forEach { initializedService ->
                    try {
                        initializedService.shutdown()
                    } catch (cleanupFailure: Throwable) {
                        failure.addSuppressed(cleanupFailure)
                    }
                }
                throw failure
            }
        }
    }

    fun shutdown() {
        val registered = mutableListOf<PlatformService>()
        registry.forEachService { registered.add(it) }
        var firstFailure: Throwable? = null
        registered.asReversed().forEach { service ->
            try {
                service.shutdown()
            } catch (failure: Throwable) {
                if (firstFailure == null) {
                    firstFailure = failure
                } else {
                    firstFailure?.addSuppressed(failure)
                }
            }
        }
        firstFailure?.let { throw it }
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