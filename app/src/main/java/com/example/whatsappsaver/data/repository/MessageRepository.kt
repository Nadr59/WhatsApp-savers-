package com.example.whatsappsaver.data.repository

import com.example.whatsappsaver.data.local.dao.MessageDao
import com.example.whatsappsaver.data.local.dao.AiHistoryDao
import com.example.whatsappsaver.data.local.entity.AiHistory
import com.example.whatsappsaver.data.local.entity.Message
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MessageRepository @Inject constructor(
    private val messageDao: MessageDao,
    private val aiHistoryDao: AiHistoryDao
) {
    fun getAllMessages() = messageDao.getAllMessages()
    fun searchMessages(query: String) = messageDao.searchMessages(query)
    fun getMessagesByCategory(category: String) = messageDao.getMessagesByCategory(category)
    fun getAllCategories() = messageDao.getAllCategories()
    suspend fun getMessageById(id: Int) = messageDao.getMessageById(id)
    suspend fun insertMessage(message: Message) = messageDao.insertMessage(message)
    suspend fun updateMessage(message: Message) = messageDao.updateMessage(message)
    suspend fun deleteMessage(message: Message) = messageDao.deleteMessage(message)
    suspend fun togglePinStatus(message: Message) {
        messageDao.updatePinStatus(message.id, !message.isPinned)
    }
    suspend fun deleteByIds(ids: List<Int>) = messageDao.deleteByIds(ids)
    suspend fun updatePinStatusByIds(ids: List<Int>, isPinned: Boolean) =
        messageDao.updatePinStatusByIds(ids, isPinned)
    suspend fun updateMessageContent(id: Int, text: String, category: String, notes: String) =
        messageDao.updateMessageContent(id, text, category, notes)
    suspend fun getAllMessagesOnce() = messageDao.getAllMessagesOnce()

    // ═══ سجل AI ═══
    fun getAiHistory(): Flow<List<AiHistory>> = aiHistoryDao.getAll()
    suspend fun insertAiHistory(history: AiHistory) = aiHistoryDao.insert(history)
    suspend fun deleteAiHistory(history: AiHistory) = aiHistoryDao.delete(history)
    suspend fun clearAiHistory() = aiHistoryDao.deleteAll()
}
