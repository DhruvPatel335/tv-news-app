package com.dhruvpatel.tvnews.presentation.news.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dhruvpatel.tvnews.domain.usecase.FetchTopNewsUseCase
import com.dhruvpatel.tvnews.domain.usecase.GetNewsUseCase
import com.dhruvpatel.tvnews.domain.usecase.RefreshNewsUseCase
import com.dhruvpatel.tvnews.presentation.news.model.NewsState
import com.dhruvpatel.tvnews.presentation.news.model.NewsUiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NewsViewModel @Inject constructor(
    private val getNewsUseCase: GetNewsUseCase,
    private val refreshNewsUseCase: RefreshNewsUseCase,
    private val fetchTopNewsUseCase: FetchTopNewsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(NewsState())
    val state: StateFlow<NewsState> = _state.asStateFlow()

    private val _eventFlow = MutableSharedFlow<NewsUiEvent>()
    val eventFlow: SharedFlow<NewsUiEvent> = _eventFlow.asSharedFlow()

    init {
        getArticles()
        // Initial load: Fetch only Top News
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val result = fetchTopNewsUseCase()
            _state.value = _state.value.copy(isLoading = false)
            
            handleResult(result)
        }
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
        // Comprehensive refresh: Parallel fetching
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val result = refreshNewsUseCase()
            _state.value = _state.value.copy(isLoading = false)
            
            handleResult(result)
        }
    }

    private suspend fun handleResult(result: Result<Unit>) {
        result.onFailure { error ->
            if (_state.value.articles.isEmpty()) {
                _state.value = _state.value.copy(error = error.message ?: "Failed to fetch news")
            } else {
                _eventFlow.emit(NewsUiEvent.ShowToast(error.message ?: "Failed to update news. Showing cached data."))
            }
        }
    }
}
