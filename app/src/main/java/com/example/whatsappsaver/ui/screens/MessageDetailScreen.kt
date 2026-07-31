package com.example.whatsappsaver.ui.screens
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.whatsappsaver.ui.viewmodel.MessageViewModel
import java.text.SimpleDateFormat
import java.util.*
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageDetailScreen(navController: NavController, messageId: Int, vm: MessageViewModel = hiltViewModel()) {
    val msg by vm.getMessageById(messageId).collectAsState(initial = null)
    val clip = LocalClipboardManager.current
    var del by remember { mutableStateOf(false) }
    msg?.let { m ->
        Scaffold(topBar = { TopAppBar(title = { Text("تفاصيل") }, navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Default.ArrowBack, "رجوع") } }, actions = { IconButton(onClick = { vm.togglePin(m) }) { Icon(Icons.Default.PushPin, "تثبيت") }; IconButton(onClick = { clip.setText(AnnotatedString(m.messageText)) }) { Icon(Icons.Default.ContentCopy, "نسخ") }; IconButton(onClick = { del = true }) { Icon(Icons.Default.Delete, "حذف", tint = MaterialTheme.colorScheme.error) } }, colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary, titleContentColor = MaterialTheme.colorScheme.onPrimary, navigationIconContentColor = MaterialTheme.colorScheme.onPrimary, actionIconContentColor = MaterialTheme.colorScheme.onPrimary)) }) { p ->
            Column(Modifier.fillMaxSize().padding(p).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                AssistChip(onClick = {}, label = { Text(m.category) }, leadingIcon = { Icon(Icons.Default.Label, null, Modifier.size(18.dp)) })
                Card(Modifier.fillMaxWidth()) { Text(m.messageText, Modifier.padding(16.dp)) }
                if (m.notes.isNotBlank()) { Text("الملاحظات:", color = MaterialTheme.colorScheme.primary); Card(Modifier.fillMaxWidth()) { Text(m.notes, Modifier.padding(16.dp)) } }
                Spacer(Modifier.weight(1f))
                Text(SimpleDateFormat("dd MMMM yyyy HH:mm", Locale("ar")).format(Date(m.timestamp)), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        if (del) AlertDialog(onDismissRequest = { del = false }, title = { Text("حذف") }, text = { Text("تأكيد الحذف؟") }, confirmButton = { TextButton(onClick = { vm.deleteMessage(m); del = false; navController.popBackStack() }) { Text("حذف", color = MaterialTheme.colorScheme.error) } }, dismissButton = { TextButton(onClick = { del = false }) { Text("إلغاء") } })
    } ?: Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
}
