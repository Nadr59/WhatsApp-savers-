package com.example.whatsappsaver.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.whatsappsaver.ui.viewmodel.MessageViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMessageScreen(
    navController: NavController,
    sharedText: String = "",
    editId: Int = -1,
    viewModel: MessageViewModel
) {
    val isEditMode = editId > 0

    var messageText by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("عام") }
    var notes by remember { mutableStateOf("") }
    var loaded by remember { mutableStateOf(false) }
    val categories = listOf("عام", "عمل", "عائلة", "أصدقاء", "مهم", "أخرى")

    // ═══ تحميل بيانات التعديل من قاعدة البيانات ═══
    if (isEditMode && !loaded) {
        LaunchedEffect(editId) {
            val msg = viewModel.getMessageByIdOnce(editId)
            if (msg != null) {
                messageText = msg.messageText
                category = msg.category
                notes = msg.notes
            }
            loaded = true
        }
    }

    // ═══ تحميل النص المشارك ═══
    LaunchedEffect(Unit) {
        if (!isEditMode && sharedText.isNotBlank() && messageText.isBlank()) {
            messageText = sharedText
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (isEditMode) "تعديل الرسالة" else "حفظ رسالة")
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "رجوع")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("الرسالة", style = MaterialTheme.typography.titleMedium)

            OutlinedTextField(
                value = messageText,
                onValueChange = { messageText = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 100.dp, max = 200.dp),
                placeholder = { Text("الرسالة المنسوخة...") },
                maxLines = 8,
                shape = RoundedCornerShape(12.dp)
            )

            Text("التصنيف", style = MaterialTheme.typography.titleMedium)

            val rows = categories.chunked(3)
            rows.forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowItems.forEach { cat ->
                        val selected = category == cat
                        FilterChip(
                            selected = selected,
                            onClick = { category = cat },
                            label = { Text(cat) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                    repeat(3 - rowItems.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }

            Text("ملاحظات", style = MaterialTheme.typography.titleMedium)

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 60.dp, max = 120.dp),
                placeholder = { Text("ملاحظات اختيارية...") },
                maxLines = 3,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    if (messageText.isNotBlank()) {
                        if (isEditMode) {
                            viewModel.updateMessage(editId, messageText, category, notes)
                        } else {
                            viewModel.addMessage(messageText, category, notes)
                        }
                        navController.popBackStack()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = messageText.isNotBlank(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    if (isEditMode) "حفظ التعديلات" else "حفظ الرسالة",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
