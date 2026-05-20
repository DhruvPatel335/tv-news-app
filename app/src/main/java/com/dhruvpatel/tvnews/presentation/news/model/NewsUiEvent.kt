package com.dhruvpatel.tvnews.presentation.news.model

sealed class NewsUiEvent {
    data class ShowToast(val message: String) : NewsUiEvent()
}
