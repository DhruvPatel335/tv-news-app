package com.dhruvpatel.tvnews.domain.usecase

import com.dhruvpatel.tvnews.domain.repository.NewsRepository
import javax.inject.Inject
import kotlinx.coroutines.CancellationException

class FetchTopNewsUseCase @Inject constructor(
    private val repository: NewsRepository,
) {
    suspend operator fun invoke(): Result<Unit> {
        return try {
            repository.fetchTopNews()
            Result.success(Unit)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(e)
        }
    }
}
