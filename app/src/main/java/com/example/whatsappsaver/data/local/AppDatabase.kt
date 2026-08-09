package com.example.whatsappsaver.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.whatsappsaver.data.local.dao.AiHistoryDao
import com.example.whatsappsaver.data.local.dao.MessageDao
import com.example.whatsappsaver.data.local.entity.AiHistory
import com.example.whatsappsaver.data.local.entity.Message

@Database(
    entities = [Message::class, AiHistory::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun messageDao(): MessageDao
    abstract fun aiHistoryDao(): AiHistoryDao
}
