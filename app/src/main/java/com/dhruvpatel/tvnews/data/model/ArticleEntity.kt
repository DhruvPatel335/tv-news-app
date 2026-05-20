package com.dhruvpatel.tvnews.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.dhruvpatel.tvnews.domain.model.Article
import com.dhruvpatel.tvnews.domain.model.Source

@Entity(tableName = "articles")
data class ArticleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val author: String?,
    val title: String,
    val description: String?,
    val url: String,
    val urlToImage: String?,
    val publishedAt: String,
    val content: String?,
    val sourceName: String
)

fun ArticleEntity.toDomainArticle(): Article {
    return Article(
        author = author,
        title = title,
        description = description,
        url = url,
        urlToImage = urlToImage,
        publishedAt = publishedAt,
        content = content,
        source = Source(id = null, name = sourceName)
    )
}

fun ArticleDto.toArticleEntity(): ArticleEntity {
    return ArticleEntity(
        author = author,
        title = title,
        description = description,
        url = url,
        urlToImage = urlToImage,
        publishedAt = publishedAt,
        content = content,
        sourceName = source.name
    )
}
