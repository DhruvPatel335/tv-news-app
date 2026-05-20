package com.dhruvpatel.tvnews.domain.usecase

import com.dhruvpatel.tvnews.common.network.safeApiCall
import com.dhruvpatel.tvnews.domain.repository.NewsRepository
import javax.inject.Inject

class RefreshNewsUseCase @Inject constructor(
    private val repository: NewsRepository,
) {
    suspend operator fun invoke(): Result<Unit> {
        return safeApiCall {
            repository.refreshNews()
        }
    }
}
