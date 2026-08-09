package com.example.whatsappsaver.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ai_history")
data class AiHistory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val originalText: String,
    val task: String,
    val result: String,
    val timestamp: Long = System.currentTimeMillis()
)
