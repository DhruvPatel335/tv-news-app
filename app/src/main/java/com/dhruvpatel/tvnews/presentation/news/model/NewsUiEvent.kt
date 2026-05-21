package com.dhruvpatel.tvnews.presentation.news.model

import com.dhruvpatel.tvnews.common.UiText

sealed class NewsUiEvent {
    data class ShowToast(val message: UiText) : NewsUiEvent()
}
