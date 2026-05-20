package com.dhruvpatel.tvnews.data.source

import com.dhruvpatel.tvnews.data.model.NewsResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsApiService {
    
    @GET("v2/top-headlines")
    suspend fun getTopHeadlines(
        @Query("country") country: String = "us",
        @Query("category") category: String = "general",
        @Query("apiKey") apiKey: String
    ): NewsResponseDto

    companion object {
        const val BASE_URL = "https://newsapi.org/"
    }
}
