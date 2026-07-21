package com.aiaggregator.app.data

import android.content.Context
import androidx.room.*

@Database(
    entities = [ConversationEntity::class, MessageEntity::class, ClusterRecordEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun conversationDao(): ConversationDao
    abstract fun messageDao(): MessageDao
    abstract fun clusterRecordDao(): ClusterRecordDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ai_aggregator.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}