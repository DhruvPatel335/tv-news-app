package com.dhruvpatel.tvnews.domain.repository

import com.dhruvpatel.tvnews.domain.model.Article
import kotlinx.coroutines.flow.Flow

interface NewsRepository {
    fun getArticles(): Flow<List<Article>>
    suspend fun refreshNews()
}
