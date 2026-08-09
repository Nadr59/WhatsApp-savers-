package com.example.whatsappsaver.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.whatsappsaver.data.local.AiSettings
import com.example.whatsappsaver.data.local.AppDatabase
import com.example.whatsappsaver.data.local.dao.AiHistoryDao
import com.example.whatsappsaver.data.local.dao.MessageDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS ai_history (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    originalText TEXT NOT NULL,
                    task TEXT NOT NULL,
                    result TEXT NOT NULL,
                    timestamp INTEGER NOT NULL
                )
            """.trimIndent())
        }
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(context, AppDatabase::class.java, "whatsapp_saver_db")
            .addMigrations(MIGRATION_1_2)
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideMessageDao(database: AppDatabase): MessageDao = database.messageDao()

    @Provides
    fun provideAiHistoryDao(database: AppDatabase): AiHistoryDao = database.aiHistoryDao()

    @Provides
    @Singleton
    fun provideAiSettings(@ApplicationContext context: Context): AiSettings = AiSettings(context)
}
