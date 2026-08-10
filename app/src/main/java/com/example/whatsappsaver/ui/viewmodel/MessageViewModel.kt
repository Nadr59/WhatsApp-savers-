package com.example.whatsappsaver.ui.viewmodel

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.whatsappsaver.data.ai.AiRepository
import com.example.whatsappsaver.data.local.AiSettings
import com.example.whatsappsaver.data.local.entity.AiHistory
import com.example.whatsappsaver.data.local.entity.Message
import com.example.whatsappsaver.data.repository.MessageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class MessageViewModel @Inject constructor(
    application: Application,
    private val repo: MessageRepository,
    private val aiRepo: AiRepository,
    private val aiSettings: AiSettings
) : AndroidViewModel(application) {

    private val context get() = getApplication<Application>()

    // ═══ البحث والفلترة ═══
    private val _query = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _query.asStateFlow()

    private val _cat = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _cat.asStateFlow()

    // ═══ الترتيب ═══
    enum class SortOrder { NEWEST, OLDEST, ALPHABETICAL, CATEGORY }

    private val _sortOrder = MutableStateFlow(SortOrder.NEWEST)
    val sortOrder: StateFlow<SortOrder> = _sortOrder.asStateFlow()

    // ═══ التحديد المتعدد ═══
    private val _selectedIds = MutableStateFlow<Set<Int>>(emptySet())
    val selectedIds: StateFlow<Set<Int>> = _selectedIds.asStateFlow()
    val isSelectionMode: StateFlow<Boolean> = _selectedIds.map { it.isNotEmpty() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    // ═══ الرسائل ═══
    val allMessages = repo.getAllMessages()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categories = repo.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ═══ الرسائل المفلترة والمرتبة ═══
    val filteredMessages = combine(allMessages, _query, _cat, _sortOrder) { messages, q, c, sort ->
        var result = messages
        if (!c.isNullOrBlank() && c != "الكل") {
            result = result.filter { it.category == c }
        }
        if (q.isNotBlank()) {
            result = result.filter {
                it.messageText.contains(q, ignoreCase = true) ||
                it.notes.contains(q, ignoreCase = true)
            }
        }
        result = when (sort) {
            SortOrder.NEWEST -> result.sortedWith(
                compareByDescending<Message> { it.isPinned }.thenByDescending { it.timestamp }
            )
            SortOrder.OLDEST -> result.sortedWith(
                compareByDescending<Message> { it.isPinned }.thenBy { it.timestamp }
            )
            SortOrder.ALPHABETICAL -> result.sortedWith(
                compareByDescending<Message> { it.isPinned }.thenBy { it.messageText }
            )
            SortOrder.CATEGORY -> result.sortedWith(
                compareByDescending<Message> { it.isPinned }.thenBy { it.category }
            )
        }
        result
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ═══ الرسائل ═══
    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message
    fun onMessageShown() { _message.value = null }

    // ═══ AI ═══
    var aiResult by mutableStateOf<String?>(null)
        private set
    var aiLoading by mutableStateOf(false)
        private set
    var aiError by mutableStateOf<String?>(null)
        private set

    // ═══ الدوال الأساسية ═══
    fun setSearchQuery(q: String) { _query.value = q }
    fun setCategory(c: String?) { _cat.value = c }
    fun setSortOrder(order: SortOrder) { _sortOrder.value = order }

    fun insertMessage(m: Message) {
        viewModelScope.launch {
            repo.insertMessage(m)
            autoBackup()
        }
    }

    fun deleteMessage(m: Message) {
        viewModelScope.launch {
            repo.deleteMessage(m)
            _message.value = "تم حذف الرسالة"
            autoBackup()
        }
    }

    fun togglePin(m: Message) {
        viewModelScope.launch {
            repo.togglePinStatus(m)
        }
    }

    fun getMessageById(id: Int): Flow<Message?> = flow { emit(repo.getMessageById(id)) }

    suspend fun getMessageByIdOnce(id: Int): Message? {
        return repo.getMessageById(id)
    }

    fun addMessage(text: String, cat: String, note: String) {
        viewModelScope.launch {
            repo.insertMessage(
                Message(
                    messageText = text,
                    category = cat,
                    notes = note,
                    isPinned = false,
                    timestamp = System.currentTimeMillis()
                )
            )
            _message.value = "تم حفظ الرسالة"
            autoBackup()
        }
    }

    fun updateMessage(id: Int, text: String, cat: String, note: String) {
        viewModelScope.launch {
            repo.updateMessageContent(id, text, cat, note)
            _message.value = "تم تعديل الرسالة"
            autoBackup()
        }
    }

    // ═══ التحديد المتعدد ═══
    fun toggleSelection(id: Int) {
        _selectedIds.value = _selectedIds.value.let { current ->
            if (id in current) current - id else current + id
        }
    }

    fun selectAll() {
        _selectedIds.value = allMessages.value.map { it.id }.toSet()
    }

    fun clearSelection() {
        _selectedIds.value = emptySet()
    }

    // ═══ عمليات متعددة ═══
    fun deleteSelected() {
        val ids = _selectedIds.value.toList()
        if (ids.isEmpty()) return
        viewModelScope.launch {
            repo.deleteByIds(ids)
            _selectedIds.value = emptySet()
            _message.value = "تم حذف ${ids.size} رسالة"
            autoBackup()
        }
    }

    fun pinSelected() {
        val ids = _selectedIds.value.toList()
        if (ids.isEmpty()) return
        viewModelScope.launch {
            repo.updatePinStatusByIds(ids, true)
            _selectedIds.value = emptySet()
            _message.value = "تم تثبيت ${ids.size} رسالة"
        }
    }

    fun unpinSelected() {
        val ids = _selectedIds.value.toList()
        if (ids.isEmpty()) return
        viewModelScope.launch {
            repo.updatePinStatusByIds(ids, false)
            _selectedIds.value = emptySet()
            _message.value = "تم إلغاء تثبيت ${ids.size} رسالة"
        }
    }

    fun shareSelected() {
        val ids = _selectedIds.value
        if (ids.isEmpty()) return
        val msgs = allMessages.value.filter { it.id in ids }
        val text = msgs.joinToString("\n\n---\n\n") { it.messageText }
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(
            Intent.createChooser(intent, "مشاركة الرسائل")
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
        _selectedIds.value = emptySet()
    }

    // ═══ نسخ للحافظة ═══
    fun copyToClipboard(text: String) {
        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.setPrimaryClip(ClipData.newPlainText("message", text))
        _message.value = "تم النسخ!"
    }

    // ═══ مشاركة رسالة واحدة ═══
    fun shareMessage(text: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(
            Intent.createChooser(intent, "مشاركة")
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }

    // ═══ التصدير/النسخ الاحتياطي ═══
    fun exportAllMessages(): String {
        val messages = allMessages.value
        if (messages.isEmpty()) return "لا توجد رسائل للتصدير"

        val json = JSONObject().apply {
            put("app", "WhatsAppSaver")
            put("version", 1)
            put("date", System.currentTimeMillis())
            put("count", messages.size)

            val arr = JSONArray()
            for (m in messages) {
                arr.put(JSONObject().apply {
                    put("text", m.messageText)
                    put("category", m.category)
                    put("notes", m.notes)
                    put("pinned", m.isPinned)
                    put("timestamp", m.timestamp)
                })
            }
            put("messages", arr)
        }

        return try {
            val dir = File(context.getExternalFilesDir(null), "backups")
            dir.mkdirs()
            val dateStr = SimpleDateFormat("yyyy-MM-dd_HH-mm", Locale.getDefault()).format(Date())
            val file = File(dir, "backup_$dateStr.json")
            file.writeText(json.toString(2))
            file.absolutePath
        } catch (e: Exception) {
            "خطأ: ${e.message}"
        }
    }

    fun shareBackupFile(): Uri? {
        val path = exportAllMessages()
        if (path.startsWith("لا") || path.startsWith("خطأ")) return null
        return try {
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                File(path)
            )
        } catch (_: Exception) { null }
    }

    // ═══ الاستيراد ═══
    fun importFromJson(jsonText: String): Int {
        return try {
            val json = JSONObject(jsonText)
            val arr = json.getJSONArray("messages")
            viewModelScope.launch {
                var count = 0
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    repo.insertMessage(
                        Message(
                            messageText = obj.getString("text"),
                            category = obj.optString("category", "عام"),
                            notes = obj.optString("notes", ""),
                            isPinned = obj.optBoolean("pinned", false),
                            timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                        )
                    )
                    count++
                }
                _message.value = "تم استيراد $count رسالة"
            }
            arr.length()
        } catch (_: Exception) {
            _message.value = "خطأ في الاستيراد"
            0
        }
    }

    // ═══ نسخ احتياطي تلقائي ═══
    private fun autoBackup() {
        viewModelScope.launch {
            try {
                val messages = repo.getAllMessagesOnce()
                if (messages.isEmpty()) return@launch

                val json = JSONObject().apply {
                    put("app", "WhatsAppSaver")
                    put("version", 1)
                    put("date", System.currentTimeMillis())
                    put("count", messages.size)
                    val arr = JSONArray()
                    for (m in messages) {
                        arr.put(JSONObject().apply {
                            put("text", m.messageText)
                            put("category", m.category)
                            put("notes", m.notes)
                            put("pinned", m.isPinned)
                            put("timestamp", m.timestamp)
                        })
                    }
                    put("messages", arr)
                }

                val dir = File(context.getExternalFilesDir(null), "backups")
                dir.mkdirs()
                val file = File(dir, "auto_backup.json")
                file.writeText(json.toString(2))
            } catch (_: Exception) {}
        }
    }

    // ═══════════════════════════════════════════════
    // ═══ معالجة AI — دالة واحدة فقط لكل عملية ═══
    // ═══════════════════════════════════════════════

    fun processWithAi(text: String, task: String) {
        viewModelScope.launch {
            aiLoading = true
            aiError = null
            aiResult = null
            val result = aiRepo.process(text, task)
            result.fold(
                onSuccess = { response ->
                    aiResult = response
                    repo.insertAiHistory(
                        AiHistory(
                            originalText = text,
                            task = task,
                            result = response
                        )
                    )
                },
                onFailure = { e ->
                    aiError = e.message ?: "خطأ غير معروف"
                }
            )
            aiLoading = false
        }
    }

    fun clearAiResult() {
        aiResult = null
        aiError = null
    }

    fun getAiHistory() = repo.getAiHistory()

    fun deleteAiHistoryItem(item: AiHistory) {
        viewModelScope.launch { repo.deleteAiHistory(item) }
    }

    fun clearAiHistory() {
        viewModelScope.launch { repo.clearAiHistory() }
    }

    // ═══════════════════════════════════════════════
    // ═══ إعدادات AI — دالة واحدة فقط لكل مزود ═══
    // ═══════════════════════════════════════════════

    fun getAiSettings() = aiSettings

    fun saveAiProvider(provider: String) {
        aiSettings.provider = provider
    }

    fun saveOpenAiKey(key: String) {
        aiSettings.openaiKey = key
    }

    fun saveGeminiKey(key: String) {
        aiSettings.geminiKey = key
    }

    fun saveMistralKey(key: String) {
        aiSettings.mistralKey = key
    }

    fun saveOpenRouterConfig(key: String, model: String) {
        aiSettings.openrouterKey = key
        aiSettings.openrouterModel = model
    }

    fun saveGroqConfig(key: String, model: String) {
        aiSettings.groqKey = key
        aiSettings.groqModel = model
    }

    fun saveCustomConfig(url: String, key: String, model: String) {
        aiSettings.customUrl = url
        aiSettings.customKey = key
        aiSettings.customModel = model
    }
}
