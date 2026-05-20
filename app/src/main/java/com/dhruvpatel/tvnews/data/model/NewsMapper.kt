package com.dhruvpatel.tvnews.data.model

import com.dhruvpatel.tvnews.domain.model.Article
import com.dhruvpatel.tvnews.domain.model.Source

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
