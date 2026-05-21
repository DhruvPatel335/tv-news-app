package com.dhruvpatel.tvnews.common.network

import com.dhruvpatel.tvnews.R
import com.dhruvpatel.tvnews.common.UiText
import kotlinx.coroutines.delay
import java.io.IOException
import retrofit2.HttpException

/**
 * Custom exception that carries a [UiText] for user-friendly error reporting.
 */
class AppException(val uiText: UiText) : Exception()

/**
 * Retries a suspend function with exponential backoff.
 */
suspend fun <T> retry(
    times: Int = 3,
    initialDelay: Long = 1000,
    maxDelay: Long = 10000,
    factor: Double = 2.0,
    block: suspend () -> T
): T {
    var currentDelay = initialDelay
    repeat(times - 1) { attempt ->
        try {
            return block()
        } catch (e: Exception) {
            if (e !is IOException && e !is HttpException) {
                throw e
            }
        }
        delay(currentDelay)
        currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelay)
    }
    return block()
}

/**
 * Wraps an API call and maps common network exceptions to [AppException].
 */
suspend fun <T> safeApiCall(
    block: suspend () -> T
): Result<T> {
    return try {
        Result.success(block())
    } catch (e: IOException) {
        Result.failure(AppException(UiText.StringResource(R.string.error_no_internet)))
    } catch (e: HttpException) {
        val uiText = when (e.code()) {
            401 -> UiText.StringResource(R.string.error_unauthorized)
            404 -> UiText.StringResource(R.string.error_not_found)
            429 -> UiText.StringResource(R.string.error_too_many_requests)
            in 500..599 -> UiText.StringResource(R.string.error_server_error)
            else -> UiText.StringResource(R.string.error_unknown)
        }
        Result.failure(AppException(uiText))
    } catch (e: Exception) {
        Result.failure(e)
    }
}
