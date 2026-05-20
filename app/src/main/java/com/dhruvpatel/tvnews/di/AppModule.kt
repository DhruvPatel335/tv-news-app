package com.dhruvpatel.tvnews.di

import android.app.Application
import androidx.room.Room
import com.dhruvpatel.tvnews.data.repository.NewsRepositoryImpl
import com.dhruvpatel.tvnews.data.source.NewsApiService
import com.dhruvpatel.tvnews.data.source.NewsDao
import com.dhruvpatel.tvnews.data.source.NewsDatabase
import com.dhruvpatel.tvnews.domain.repository.NewsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()
    }

    @Provides
    @Singleton
    fun provideNewsApiService(okHttpClient: OkHttpClient): NewsApiService {
        return Retrofit.Builder()
            .baseUrl(NewsApiService.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NewsApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideNewsDatabase(app: Application): NewsDatabase {
        return Room.databaseBuilder(
            app,
            NewsDatabase::class.java,
            NewsDatabase.DATABASE_NAME
        ).build()
    }

    @Provides
    @Singleton
    fun provideNewsDao(db: NewsDatabase): NewsDao {
        return db.newsDao
    }

    @Provides
    @Singleton
    fun provideNewsRepository(
        apiService: NewsApiService,
        dao: NewsDao
    ): NewsRepository {
        return NewsRepositoryImpl(apiService, dao)
    }
}
