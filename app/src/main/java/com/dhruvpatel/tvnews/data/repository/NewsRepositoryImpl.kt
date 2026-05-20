package com.dhruvpatel.tvnews.data.repository

import com.dhruvpatel.tvnews.BuildConfig
import com.dhruvpatel.tvnews.common.network.ConnectivityObserver
import com.dhruvpatel.tvnews.common.network.retry
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.IOException
import javax.inject.Inject

class NewsRepositoryImpl @Inject constructor(
    private val apiService: NewsApiService,
    private val dao: NewsDao,
    private val connectivityObserver: ConnectivityObserver
) : NewsRepository {

    private val refreshMutex = Mutex()
    private var refreshCount = 0

    override fun getArticles(): Flow<List<Article>> {
        return dao.getArticles().map { entities ->
            entities.map { it.toDomainArticle() }
        }
    }

    override suspend fun fetchTopNews() {
        checkConnectivity()
        refreshMutex.withLock {
            val response = retry {
                apiService.getTopHeadlines(apiKey = BuildConfig.NEWS_API_KEY)
            }
            val entities = response.articles.map { it.toArticleEntity() }
            dao.clearArticles()
            dao.insertArticles(entities)
            refreshCount++
        }
    }

    override suspend fun refreshNews() {
        checkConnectivity()
        refreshMutex.withLock {
            coroutineScope {
                val categories = listOf("business", "technology", "science")

                val results = categories.map { category ->
                    async {
                        runCatching {
                            retry {
                                apiService.getTopHeadlines(
                                    category = category,
                                    apiKey = BuildConfig.NEWS_API_KEY
                                ).articles.map { it.toArticleEntity() }
                            }
                        }
                    }
                }.awaitAll()

                val allArticles = results.mapNotNull { it.getOrNull() }.flatten()
                val failures = results.filter { it.isFailure }

                if (allArticles.isNotEmpty()) {
                    dao.clearArticles()
                    dao.insertArticles(allArticles)
                    refreshCount++
                }

                if (failures.isNotEmpty()) {
                    throw failures.first().exceptionOrNull()!!
                }
            }
        }
    }

    private suspend fun checkConnectivity() {
        val status = connectivityObserver.observe().first()
        if (status != ConnectivityObserver.Status.Available) {
            throw IOException("No internet connection")
        }
    }
}
