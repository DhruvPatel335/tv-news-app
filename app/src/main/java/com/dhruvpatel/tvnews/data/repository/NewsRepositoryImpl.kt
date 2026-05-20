package com.dhruvpatel.tvnews.data.repository

import com.dhruvpatel.tvnews.BuildConfig
import com.dhruvpatel.tvnews.data.local.NewsDao
import com.dhruvpatel.tvnews.data.mapper.toArticleEntity
import com.dhruvpatel.tvnews.data.mapper.toDomainArticle
import com.dhruvpatel.tvnews.data.remote.NewsApiService
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
        val response = apiService.getTopHeadlines(apiKey = BuildConfig.NEWS_API_KEY)
        val entities = response.articles.map { it.toArticleEntity() }
        dao.clearArticles()
        dao.insertArticles(entities)
    }
}
