package com.example.whatsappsaver.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.whatsappsaver.ui.viewmodel.MessageViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMessageScreen(
    navController: NavController,
    sharedText: String = "",
    viewModel: MessageViewModel = hiltViewModel()
) {
    var messageText by remember { mutableStateOf(sharedText) }
    var category by remember { mutableStateOf("عام") }
    var notes by remember { mutableStateOf("") }
    val categories = listOf("عام", "عمل", "عائلة", "أصدقاء", "مهم", "أخرى")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("حفظ رسالة") },
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

            // ===== الرسالة =====
            Text("الرسالة", style = MaterialTheme.typography.titleMedium)

            OutlinedTextField(
                value = messageText,
                onValueChange = { messageText = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 100.dp, max = 200.dp),  // حد أقصى للارتفاع
                placeholder = { Text("الرسالة المنسوخة...") },
                maxLines = 8
            )

            // ===== التصنيف =====
            Text("التصنيف", style = MaterialTheme.typography.titleMedium)

            // صفوف الأزرار
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
                            modifier = Modifier.weight(1f)
                        )
                    }
                    // ملء الفراغ لو العناصر أقل من 3
                    repeat(3 - rowItems.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }

            // ===== الملاحظات =====
            Text("ملاحظات", style = MaterialTheme.typography.titleMedium)

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 60.dp, max = 120.dp),
                placeholder = { Text("ملاحظات اختيارية...") },
                maxLines = 3
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ===== زر الحفظ =====
            Button(
                onClick = {
                    if (messageText.isNotBlank()) {
                        viewModel.addMessage(
                            text = messageText,
                            cat = category,
                            note = notes
                        )
                        navController.popBackStack()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = messageText.isNotBlank()
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("حفظ الرسالة", style = MaterialTheme.typography.titleMedium)
            }

            // مساحة في الأسفل عشان زر الحفظ ما يختفى
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
