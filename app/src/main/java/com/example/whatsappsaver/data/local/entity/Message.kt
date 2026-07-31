package com.example.whatsappsaver.data.local.entity
import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity(tableName = "messages")
data class Message(@PrimaryKey(autoGenerate = true) val id: Int = 0, val messageText: String, val notes: String = "", val category: String = "عام", val timestamp: Long = System.currentTimeMillis(), val isPinned: Boolean = false)
