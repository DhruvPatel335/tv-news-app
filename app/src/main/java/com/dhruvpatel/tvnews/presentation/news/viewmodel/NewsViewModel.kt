package com.dhruvpatel.tvnews.presentation.news.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dhruvpatel.tvnews.R
import com.dhruvpatel.tvnews.common.UiText
import com.dhruvpatel.tvnews.common.network.AppException
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
class  NewsViewModel @Inject constructor(
    private val getNewsUseCase: GetNewsUseCase,
    private val refreshNewsUseCase: RefreshNewsUseCase,
    private val fetchTopNewsUseCase: FetchTopNewsUseCase
) : ViewModel() {

    // Internal mutable state for the UI, exposed as a read-only StateFlow
    private val _state = MutableStateFlow(NewsState())
    val state: StateFlow<NewsState> = _state.asStateFlow()

    // SharedFlow for one-time UI events like showing Toasts
    private val _eventFlow = MutableSharedFlow<NewsUiEvent>()
    val eventFlow: SharedFlow<NewsUiEvent> = _eventFlow.asSharedFlow()

    init {
        // Start observing local database changes immediately
        getArticles()
        
        // Initial load: Fetch only Top News from the API
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val result = fetchTopNewsUseCase()
            _state.value = _state.value.copy(isLoading = false)
            
            handleResult(result)
        }
    }

    /**
     * Observes the local database for news articles and updates the state.
     * This provides an offline-first experience.
     */
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

    /**
     * Triggers a comprehensive refresh by fetching both top news and categorized news.
     */
    fun refreshNews() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val result = refreshNewsUseCase()
            _state.value = _state.value.copy(isLoading = false)
            
            handleResult(result)
        }
    }

    private suspend fun handleResult(result: Result<Unit>) {
        result.onFailure { error ->
            val uiText = (error as? AppException)?.uiText 
                ?: UiText.StringResource(R.string.failed_to_fetch_news)
            
            if (_state.value.articles.isEmpty()) {
                _state.value = _state.value.copy(error = uiText)
            } else {
                val toastUiText = (error as? AppException)?.uiText 
                    ?: UiText.StringResource(R.string.failed_to_update_news)
                _eventFlow.emit(NewsUiEvent.ShowToast(toastUiText))
            }
        }
    }
}
