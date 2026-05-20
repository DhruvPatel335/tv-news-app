package com.dhruvpatel.tvnews.presentation.news

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.dhruvpatel.tvnews.presentation.news.components.ArticleItem

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun NewsScreen(
    viewModel: NewsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        if (state.isLoading) {
            Text(
                text = "Loading...",
                modifier = Modifier.align(Alignment.Center),
                style = MaterialTheme.typography.displayMedium
            )
        } else if (state.error != null) {
            Text(
                text = state.error ?: "Unknown Error",
                modifier = Modifier.align(Alignment.Center),
                color = MaterialTheme.colorScheme.error
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 96.dp, vertical = 64.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                item {
                    Text(
                        text = "News Headlines",
                        style = MaterialTheme.typography.displayMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }
                items(state.articles) { article ->
                    ArticleItem(article = article)
                }
            }
        }
    }
}
