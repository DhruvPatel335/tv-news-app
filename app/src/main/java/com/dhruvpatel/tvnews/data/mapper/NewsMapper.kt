package com.dhruvpatel.tvnews.data.mapper

import com.dhruvpatel.tvnews.data.local.entity.ArticleEntity
import com.dhruvpatel.tvnews.data.remote.dto.ArticleDto
import com.dhruvpatel.tvnews.data.remote.dto.SourceDto
import com.dhruvpatel.tvnews.domain.model.Article
import com.dhruvpatel.tvnews.domain.model.Source

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

fun ArticleDto.toDomainArticle(): Article {
    return Article(
        author = author,
        title = title,
        description = description,
        url = url,
        urlToImage = urlToImage,
        publishedAt = publishedAt,
        content = content,
        source = source.toDomainSource()
    )
}

fun SourceDto.toDomainSource(): Source {
    return Source(
        id = id,
        name = name
    )
}

