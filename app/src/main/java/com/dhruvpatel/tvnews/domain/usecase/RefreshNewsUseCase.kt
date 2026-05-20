package com.dhruvpatel.tvnews.domain.usecase

import com.dhruvpatel.tvnews.domain.repository.NewsRepository
import javax.inject.Inject
import kotlinx.coroutines.CancellationException

class RefreshNewsUseCase @Inject constructor(
    private val repository: NewsRepository,
) {
    suspend operator fun invoke(): Result<Unit> {
        return try {
            repository.refreshNews()
            Result.success(Unit)
        } catch (e: CancellationException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
