package com.dhruvpatel.tvnews.common.network

import kotlinx.coroutines.delay
import java.io.IOException
import retrofit2.HttpException

/**
 * Retries a suspend function with exponential backoff.
 */
suspend fun <T> retry(
    times: Int = 3,
    initialDelay: Long = 1000, // 1 second
    maxDelay: Long = 10000,    // 10 seconds
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
            // Log or print attempt if needed
        }
        delay(currentDelay)
        currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelay)
    }
    return block() // final attempt
}

/**
 * Wraps an API call and maps exceptions to user-friendly messages.
 */
suspend fun <T> safeApiCall(
    block: suspend () -> T
): Result<T> {
    return try {
        Result.success(block())
    } catch (e: IOException) {
        Result.failure(Exception("No internet connection. Please check your network.", e))
    } catch (e: HttpException) {
        val errorMessage = when (e.code()) {
            401 -> "Unauthorized. Please check your API key."
            404 -> "Resource not found."
            429 -> "Too many requests. Please try again later."
            in 500..599 -> "Server error. Please try again later."
            else -> "Something went wrong. Please try again."
        }
        Result.failure(Exception(errorMessage, e))
    } catch (e: Exception) {
        Result.failure(e)
    }
}
