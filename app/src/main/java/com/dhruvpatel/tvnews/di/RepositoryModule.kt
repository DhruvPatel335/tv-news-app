package com.dhruvpatel.tvnews.di

import com.dhruvpatel.tvnews.common.network.ConnectivityObserver
import com.dhruvpatel.tvnews.data.local.NewsDao
import com.dhruvpatel.tvnews.data.remote.NewsApiService
import com.dhruvpatel.tvnews.data.repository.NewsRepositoryImpl
import com.dhruvpatel.tvnews.domain.repository.NewsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideNewsRepository(
        apiService: NewsApiService,
        dao: NewsDao,
        connectivityObserver: ConnectivityObserver
    ): NewsRepository {
        return NewsRepositoryImpl(apiService, dao, connectivityObserver)
    }
}
