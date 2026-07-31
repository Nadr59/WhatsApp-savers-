package com.example.whatsappsaver.ui.viewmodel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.whatsappsaver.data.local.entity.Message
import com.example.whatsappsaver.data.repository.MessageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
@HiltViewModel
class MessageViewModel @Inject constructor(private val repo: MessageRepository) : ViewModel() {
    private val _query = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _query.asStateFlow()
    private val _cat = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _cat.asStateFlow()
    val allMessages = repo.getAllMessages().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val categories = repo.getAllCategories().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val filteredMessages = combine(allMessages, _query, _cat) { m, q, c -> var r = m; if (!c.isNullOrBlank() && c != "الكل") r = r.filter { it.category == c }; if (q.isNotBlank()) r = r.filter { it.messageText.contains(q, true) || it.notes.contains(q, true) }; r }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    fun setSearchQuery(q: String) { _query.value = q }
    fun setCategory(c: String?) { _cat.value = c }
    fun insertMessage(m: Message) { viewModelScope.launch { repo.insertMessage(m) } }
    fun deleteMessage(m: Message) { viewModelScope.launch { repo.deleteMessage(m) } }
    fun togglePin(m: Message) { viewModelScope.launch { repo.togglePinStatus(m) } }
    fun getMessageById(id: Int): Flow<Message?> = flow { emit(repo.getMessageById(id)) }
}
