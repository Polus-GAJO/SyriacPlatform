package org.syriacplatform.common.result

import org.syriacplatform.common.types.PlatformError

/**
 * النتيجة الموحدة لجميع العمليات داخل SyriacPlatform.
 */
sealed interface Result<out T> {

    data class Success<T>(
        val data: T
    ) : Result<T>

    data class Failure(
        val error: PlatformError
    ) : Result<Nothing>
}