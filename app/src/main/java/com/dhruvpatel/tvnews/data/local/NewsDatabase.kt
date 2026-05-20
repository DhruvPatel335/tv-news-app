package com.dhruvpatel.tvnews.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.dhruvpatel.tvnews.data.local.entity.ArticleEntity

@Database(entities = [ArticleEntity::class], version = 1, exportSchema = false)
abstract class NewsDatabase : RoomDatabase() {
    abstract val newsDao: NewsDao

    companion object {
        const val DATABASE_NAME = "news_db"
    }
}
