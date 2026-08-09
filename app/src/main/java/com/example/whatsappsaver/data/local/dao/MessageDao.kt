package com.example.whatsappsaver.data.local.dao

import androidx.room.*
import com.example.whatsappsaver.data.local.entity.Message
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {

    @Query("SELECT * FROM messages ORDER BY isPinned DESC, timestamp DESC")
    fun getAllMessages(): Flow<List<Message>>

    @Query("SELECT * FROM messages WHERE id = :id")
    suspend fun getMessageById(id: Int): Message?

    @Query("SELECT * FROM messages WHERE messageText LIKE '%' || :query || '%' OR notes LIKE '%' || :query || '%'")
    fun searchMessages(query: String): Flow<List<Message>>

    @Query("SELECT * FROM messages WHERE category = :category")
    fun getMessagesByCategory(category: String): Flow<List<Message>>

    @Query("SELECT DISTINCT category FROM messages")
    fun getAllCategories(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: Message): Long

    @Update
    suspend fun updateMessage(message: Message)

    @Delete
    suspend fun deleteMessage(message: Message)

    @Query("UPDATE messages SET isPinned = :isPinned WHERE id = :id")
    suspend fun updatePinStatus(id: Int, isPinned: Boolean)

    // ═══ جديد: عمليات متعددة ═══
    @Query("DELETE FROM messages WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Int>)

    @Query("UPDATE messages SET isPinned = :isPinned WHERE id IN (:ids)")
    suspend fun updatePinStatusByIds(ids: List<Int>, isPinned: Boolean)

    @Query("UPDATE messages SET messageText = :text, category = :category, notes = :notes WHERE id = :id")
    suspend fun updateMessageContent(id: Int, text: String, category: String, notes: String)

    @Query("SELECT * FROM messages")
    suspend fun getAllMessagesOnce(): List<Message>
}
