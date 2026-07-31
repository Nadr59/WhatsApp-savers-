package com.example.whatsappsaver.ui.screens
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.whatsappsaver.data.local.entity.Message
import com.example.whatsappsaver.ui.viewmodel.MessageViewModel
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMessageScreen(navController: NavController, sharedText: String = "", vm: MessageViewModel = hiltViewModel()) {
    var text by remember { mutableStateOf(sharedText) }
    var notes by remember { mutableStateOf("") }
    var cat by remember { mutableStateOf("عام") }
    var dlg by remember { mutableStateOf(false) }
    val cats by vm.categories.collectAsState()
    Scaffold(topBar = { TopAppBar(title = { Text("إضافة رسالة") }, navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Default.ArrowBack, "رجوع") } }, colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary, titleContentColor = MaterialTheme.colorScheme.onPrimary, navigationIconContentColor = MaterialTheme.colorScheme.onPrimary)) }) { p ->
        Column(Modifier.fillMaxSize().padding(p).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedTextField(text, { text = it }, label = { Text("نص الرسالة *") }, modifier = Modifier.fillMaxWidth(), minLines = 5)
            OutlinedCard(onClick = { dlg = true }, Modifier.fillMaxWidth()) { Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) { Text("التصنيف: $cat"); Icon(Icons.Default.ArrowDropDown, null) } }
            OutlinedTextField(notes, { notes = it }, label = { Text("ملاحظات") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
            Spacer(Modifier.weight(1f))
            Button(onClick = { if (text.isNotBlank()) { vm.insertMessage(Message(messageText = text.trim(), notes = notes.trim(), category = cat)); navController.popBackStack() } }, Modifier.fillMaxWidth(), enabled = text.isNotBlank()) { Icon(Icons.Default.Save, null); Spacer(Modifier.width(8.dp)); Text("حفظ") }
        }
    }
    if (dlg) AlertDialog(onDismissRequest = { dlg = false }, title = { Text("اختر التصنيف") }, text = { Column { (cats + listOf("عام","عمل","عائلة","أصدقاء","مهم")).distinct().forEach { c -> TextButton(onClick = { cat = c; dlg = false }, Modifier.fillMaxWidth()) { Text(c) } } } }, confirmButton = { TextButton(onClick = { dlg = false }) { Text("إلغاء") } })
}
