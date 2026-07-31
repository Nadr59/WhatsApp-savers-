package com.example.whatsappsaver.data.repository
import com.example.whatsappsaver.data.local.dao.MessageDao
import com.example.whatsappsaver.data.local.entity.Message
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton
@Singleton
class MessageRepository @Inject constructor(private val messageDao: MessageDao) {
    fun getAllMessages() = messageDao.getAllMessages()
    fun searchMessages(query: String) = messageDao.searchMessages(query)
    fun getMessagesByCategory(category: String) = messageDao.getMessagesByCategory(category)
    fun getAllCategories() = messageDao.getAllCategories()
    suspend fun getMessageById(id: Int) = messageDao.getMessageById(id)
    suspend fun insertMessage(message: Message) = messageDao.insertMessage(message)
    suspend fun updateMessage(message: Message) = messageDao.updateMessage(message)
    suspend fun deleteMessage(message: Message) = messageDao.deleteMessage(message)
    suspend fun togglePinStatus(message: Message) { messageDao.updatePinStatus(message.id, !message.isPinned) }
}
