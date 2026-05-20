package com.dhruvpatel.tvnews.data.repository

import com.dhruvpatel.tvnews.data.model.toArticleEntity
import com.dhruvpatel.tvnews.data.model.toDomainArticle
import com.dhruvpatel.tvnews.data.source.NewsApiService
import com.dhruvpatel.tvnews.data.source.NewsDao
import com.dhruvpatel.tvnews.domain.model.Article
import com.dhruvpatel.tvnews.domain.repository.NewsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class NewsRepositoryImpl @Inject constructor(
    private val apiService: NewsApiService,
    private val dao: NewsDao
) : NewsRepository {

    override fun getArticles(): Flow<List<Article>> {
        return dao.getArticles().map { entities ->
            entities.map { it.toDomainArticle() }
        }
    }

    override suspend fun refreshNews() {
        // Fetch from network and update cache
        val response = apiService.getTopHeadlines(apiKey = "YOUR_API_KEY")
        val entities = response.articles.map { it.toArticleEntity() }
        dao.clearArticles()
        dao.insertArticles(entities)
    }
}
