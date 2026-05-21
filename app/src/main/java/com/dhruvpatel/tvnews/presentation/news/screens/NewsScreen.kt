package com.dhruvpatel.tvnews.presentation.news.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.res.stringResource
import com.dhruvpatel.tvnews.R
import com.dhruvpatel.tvnews.presentation.news.components.ArticleItem
import com.dhruvpatel.tvnews.presentation.news.model.NewsUiEvent
import com.dhruvpatel.tvnews.presentation.news.viewmodel.NewsViewModel
import kotlinx.coroutines.flow.collectLatest

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items

import androidx.compose.runtime.remember
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.foundation.focusable

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun NewsScreen(
    viewModel: NewsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val errorFocusRequester = remember { FocusRequester() }

    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is NewsUiEvent.ShowToast -> {
                    Toast.makeText(context, event.message.asString(context), Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    /**
     * Handles the custom refresh gesture for Android TV.
     * Triggers a news refresh when the user long-presses the Direction Down key.
     */
    fun handleRefreshGesture(keyEvent: androidx.compose.ui.input.key.KeyEvent): Boolean {
        return if (keyEvent.key == Key.DirectionDown && keyEvent.nativeKeyEvent.isLongPress) {
            if (keyEvent.type == KeyEventType.KeyDown) {
                viewModel.refreshNews()
                true
            } else {
                true
            }
        } else {
            false
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        if (state.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = MaterialTheme.colorScheme.primary
            )
        } else if (state.error != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .focusRequester(errorFocusRequester)
                    .focusable()
                    .onKeyEvent { handleRefreshGesture(it) }
            ) {
                LaunchedEffect(Unit) {
                    errorFocusRequester.requestFocus()
                }
                Text(
                    text = state.error?.asString() ?: stringResource(id = R.string.unknown_error),
                    color = MaterialTheme.colorScheme.error
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .onKeyEvent { handleRefreshGesture(it) },
                contentPadding = PaddingValues(horizontal = 96.dp, vertical = 64.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                item {
                    Text(
                        text = stringResource(id = R.string.news_headlines),
                        style = MaterialTheme.typography.displayMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }
                items(
                    items = state.articles,
                    // Use article.url as a stable key to maintain scroll position and 
                    // optimize list updates when the data changes.
                    key = { article -> article.url }
                ) { article ->
                    ArticleItem(article = article)
                }
            }
        }
    }
}
