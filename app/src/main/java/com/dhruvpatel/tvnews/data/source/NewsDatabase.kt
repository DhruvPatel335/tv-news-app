package com.dhruvpatel.tvnews.data.source

import androidx.room.Database
import androidx.room.RoomDatabase
import com.dhruvpatel.tvnews.data.model.ArticleEntity

@Database(entities = [ArticleEntity::class], version = 1, exportSchema = false)
abstract class NewsDatabase : RoomDatabase() {
    abstract val newsDao: NewsDao

    companion object {
        const val DATABASE_NAME = "news_db"
    }
}
