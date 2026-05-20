package com.dhruvpatel.tvnews.presentation.news

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dhruvpatel.tvnews.domain.usecase.GetNewsUseCase
import com.dhruvpatel.tvnews.domain.usecase.RefreshNewsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NewsViewModel @Inject constructor(
    private val getNewsUseCase: GetNewsUseCase,
    private val refreshNewsUseCase: RefreshNewsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(NewsState())
    val state: StateFlow<NewsState> = _state.asStateFlow()

    init {
        getArticles()
        refreshNews()
    }

    private fun getArticles() {
        getNewsUseCase()
            .onEach { articles ->
                _state.value = _state.value.copy(
                    articles = articles,
                    error = null
                )
            }
            .launchIn(viewModelScope)
    }

    fun refreshNews() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val result = refreshNewsUseCase()
            _state.value = _state.value.copy(isLoading = false)
            
            result.onFailure { error ->
                _state.value = _state.value.copy(error = error.message ?: "Failed to refresh news")
            }
        }
    }
}
