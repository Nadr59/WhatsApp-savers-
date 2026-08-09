package com.example.whatsappsaver.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.whatsappsaver.ui.viewmodel.MessageViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AiScreen(
    navController: NavController,
    messageText: String = "",
    messageId: Int = -1,
    viewModel: MessageViewModel
) {
    var text by remember { mutableStateOf(messageText) }
    var selectedTask by remember { mutableStateOf<String?>(null) }
    var showHistory by remember { mutableStateOf(false) }

    val isProcessing = viewModel.aiLoading
    val result = viewModel.aiResult
    val error = viewModel.aiError

    val tasks = listOf(
        "شرح" to Icons.Default.Info,
        "استخراج معلومات" to Icons.Default.DataExploration,
        "تلخيص" to Icons.Default.ShortText,
        "ترجمة إلى الإنجليزية" to Icons.Default.Translate,
        "ترجمة إلى العربية" to Icons.Default.GTranslate,
        "اقتراح ردود" to Icons.Default.Reply,
        "تحليل المشاعر" to Icons.Default.EmojiEmotions,
        "تحليل الأولوية" to Icons.Default.PriorityHigh,
        "استخراج المهام" to Icons.Default.Task
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("معالجة بالذكاء الاصطناعي") },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.clearAiResult()
                        navController.popBackStack()
                    }) {
                        Icon(Icons.Default.ArrowBack, "رجوع")
                    }
                },
                actions = {
                    IconButton(onClick = { showHistory = true }) {
                        Icon(Icons.Default.History, "سجل")
                    }
                    IconButton(onClick = { navController.navigate("ai_settings") }) {
                        Icon(Icons.Default.Settings, "إعدادات")
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ═══ حقل النص ═══
            Text("النص:", fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.fillMaxWidth().heightIn(min = 80.dp, max = 200.dp),
                placeholder = { Text("النص المراد معالجته...") },
                maxLines = 8,
                shape = RoundedCornerShape(12.dp)
            )

            // ═══ اختيار المهمة ═══
            Text("اختر المهمة:", fontWeight = FontWeight.Bold)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                tasks.forEach { (taskName, icon) ->
                    FilterChip(
                        selected = selectedTask == taskName,
                        onClick = { selectedTask = taskName },
                        label = { Text(taskName, style = MaterialTheme.typography.bodySmall) },
                        leadingIcon = { Icon(icon, null, Modifier.size(16.dp)) },
                        shape = RoundedCornerShape(20.dp)
                    )
                }
            }

            // ═══ زر المعالجة ═══
            Button(
                onClick = {
                    if (text.isNotBlank() && selectedTask != null) {
                        viewModel.processWithAi(text, selectedTask!!)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                enabled = text.isNotBlank() && selectedTask != null && !isProcessing,
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(
                        Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("جاري المعالجة...")
                } else {
                    Icon(Icons.Default.AutoAwesome, null)
                    Spacer(Modifier.width(8.dp))
                    Text("معالجة", fontWeight = FontWeight.Bold)
                }
            }

            // ═══ الخطأ ═══
            if (error != null) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Error, null, tint = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.width(8.dp))
                        Text(error, color = MaterialTheme.colorScheme.error)
                    }
                }
            }

            // ═══ النتيجة ═══
            if (result != null) {
                Text("النتيجة:", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(result, Modifier.padding(16.dp))
                }

                // ═══ أزرار النتيجة ═══
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.copyToClipboard(result) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("نسخ")
                    }
                    OutlinedButton(
                        onClick = { viewModel.shareMessage(result) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Share, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("مشاركة")
                    }
                    if (messageId > 0) {
                        OutlinedButton(
                            onClick = {
                                viewModel.updateMessage(messageId, text, "عام", result)
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Save, null, Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("حفظ")
                        }
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }

    // ═══ سجل المعالجات ═══
    if (showHistory) {
        AiHistoryDialog(viewModel = viewModel, onDismiss = { showHistory = false })
    }
}

@Composable
fun AiHistoryDialog(viewModel: MessageViewModel, onDismiss: () -> Unit) {
    val history by viewModel.getAiHistory().collectAsState(initial = emptyList())

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("سجل المعالجات")
                if (history.isNotEmpty()) {
                    IconButton(onClick = { viewModel.clearAiHistory() }) {
                        Icon(Icons.Default.DeleteSweep, "حذف الكل",
                            tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        },
        text = {
            if (history.isEmpty()) {
                Text("لا يوجد سجل")
            } else {
                Column(modifier = Modifier.heightIn(max = 400.dp).verticalScroll(rememberScrollState())) {
                    history.forEach { item ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                        ) {
                            Column(Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(item.task, fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        style = MaterialTheme.typography.bodySmall)
                                    IconButton(
                                        onClick = { viewModel.deleteAiHistoryItem(item) },
                                        modifier = Modifier.size(20.dp)
                                    ) {
                                        Icon(Icons.Default.Close, null, Modifier.size(14.dp))
                                    }
                                }
                                Text(item.result, maxLines = 4,
                                    style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("إغلاق") }
        }
    )
}
