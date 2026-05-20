package com.dhruvpatel.tvnews.presentation.news

import com.dhruvpatel.tvnews.domain.model.Article

data class NewsState(
    val articles: List<Article> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
