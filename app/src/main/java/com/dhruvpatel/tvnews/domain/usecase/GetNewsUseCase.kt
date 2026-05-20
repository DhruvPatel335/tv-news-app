package com.dhruvpatel.tvnews.domain.usecase

import com.dhruvpatel.tvnews.domain.model.Article
import com.dhruvpatel.tvnews.domain.repository.NewsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetNewsUseCase @Inject constructor(
    private val repository: NewsRepository,
) {
    operator fun invoke(): Flow<List<Article>> {
        return repository.getArticles()
    }
}
