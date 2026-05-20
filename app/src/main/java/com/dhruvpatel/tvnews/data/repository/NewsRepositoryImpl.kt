package com.dhruvpatel.tvnews.data.repository

import android.util.Log
import com.dhruvpatel.tvnews.BuildConfig
import com.dhruvpatel.tvnews.data.local.NewsDao
import com.dhruvpatel.tvnews.data.mapper.toArticleEntity
import com.dhruvpatel.tvnews.data.mapper.toDomainArticle
import com.dhruvpatel.tvnews.data.remote.NewsApiService
import com.dhruvpatel.tvnews.domain.model.Article
import com.dhruvpatel.tvnews.domain.repository.NewsRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

class NewsRepositoryImpl @Inject constructor(
    private val apiService: NewsApiService,
    private val dao: NewsDao
) : NewsRepository {

    private val refreshMutex = Mutex()
    private var refreshCount = 0 // Demonstrating shared mutable state

    override fun getArticles(): Flow<List<Article>> {
        return dao.getArticles().map { entities ->
            entities.map { it.toDomainArticle() }
        }
    }

    override suspend fun fetchTopNews() {
        refreshMutex.withLock {
            val response = apiService.getTopHeadlines(apiKey = BuildConfig.NEWS_API_KEY)
            val entities = response.articles.map { it.toArticleEntity() }
            dao.clearArticles()
            dao.insertArticles(entities)
            refreshCount++
        }
    }

    override suspend fun refreshNews() {
        // Handle multiple triggers using Mutex to ensure only one refresh runs at a time
        refreshMutex.withLock {
            // Structured Concurrency: coroutineScope ensures all child coroutines finish or fail together
            coroutineScope {
                val categories = listOf("business", "technology", "science")

                // Parallelism: Launch multiple API calls concurrently
                val results = categories.map { category ->
                    async {
                        runCatching {
                            apiService.getTopHeadlines(
                                category = category,
                                apiKey = BuildConfig.NEWS_API_KEY
                            ).articles.map { it.toArticleEntity() }
                        }
                    }
                }.awaitAll()

                val allArticles = results.mapNotNull { it.getOrNull() }.flatten()
                val failures = results.filter { it.isFailure }

                // Mutex protects the database consistency and the shared refreshCount
                if (allArticles.isNotEmpty()) {
                    dao.clearArticles()
                    dao.insertArticles(allArticles)
                    refreshCount++
                }

                // If any category failed, throw the first error to notify the ViewModel
                // but only after we've saved the successful ones.
                if (failures.isNotEmpty()) {
                    throw failures.first().exceptionOrNull()!!
                }
            }
        }
    }
}
