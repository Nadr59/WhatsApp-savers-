package com.example.whatsappsaver.ui.screens

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.whatsappsaver.service.ClipboardMonitorService
import com.example.whatsappsaver.ui.navigation.Screen
import com.example.whatsappsaver.ui.viewmodel.MessageViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: MessageViewModel
) {
    val messages by viewModel.filteredMessages.collectAsState()
    val selectedIds by viewModel.selectedIds.collectAsState()
    val isSelectionMode by viewModel.isSelectionMode.collectAsState()
    val sortOrder by viewModel.sortOrder.collectAsState()
    val context = LocalContext.current
    var serviceEnabled by remember { mutableStateOf(ClipboardMonitorService.isRunning(context)) }
    var showSortMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        serviceEnabled = ClipboardMonitorService.isRunning(context)
    }

    Scaffold(
        topBar = {
            if (isSelectionMode) {
                TopAppBar(
                    title = { Text("${selectedIds.size} محدد") },
                    navigationIcon = {
                        IconButton(onClick = { viewModel.clearSelection() }) {
                            Icon(Icons.Default.Close, "إلغاء")
                        }
                    },
                    actions = {
                        IconButton(onClick = { viewModel.selectAll() }) {
                            Icon(Icons.Default.SelectAll, "تحديد الكل")
                        }
                        IconButton(onClick = { viewModel.shareSelected() }) {
                            Icon(Icons.Default.Share, "مشاركة")
                        }
                        IconButton(onClick = { viewModel.pinSelected() }) {
                            Icon(Icons.Default.PushPin, "تثبيت")
                        }
                        IconButton(onClick = { viewModel.unpinSelected() }) {
                            Icon(
                                Icons.Default.PushPin,
                                "إلغاء تثبيت",
                                modifier = Modifier.graphicsLayer { rotationZ = 45f }
                            )
                        }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                Icons.Default.Delete,
                                "حذف",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            } else {
                TopAppBar(
                    title = { Text("WhatsApp Saver") },
                    actions = {
                        IconButton(onClick = { showExportDialog = true }) {
                            Icon(Icons.Default.Backup, "نسخ احتياطي")
                        }
                        Box {
                            IconButton(onClick = { showSortMenu = true }) {
                                Icon(Icons.Default.Sort, "ترتيب")
                            }
                            DropdownMenu(
                                expanded = showSortMenu,
                                onDismissRequest = { showSortMenu = false }
                            ) {
                                SortMenuItem("الأحدث", MessageViewModel.SortOrder.NEWEST, sortOrder, viewModel) { showSortMenu = false }
                                SortMenuItem("الأقدم", MessageViewModel.SortOrder.OLDEST, sortOrder, viewModel) { showSortMenu = false }
                                SortMenuItem("أبجدي", MessageViewModel.SortOrder.ALPHABETICAL, sortOrder, viewModel) { showSortMenu = false }
                                SortMenuItem("حسب التصنيف", MessageViewModel.SortOrder.CATEGORY, sortOrder, viewModel) { showSortMenu = false }
                            }
                        }
                        IconButton(onClick = {
                            serviceEnabled = ClipboardMonitorService.isRunning(context)
                        }) {
                            Icon(
                                imageVector = if (serviceEnabled) Icons.Default.Notifications
                                else Icons.Default.NotificationsOff,
                                contentDescription = "مراقبة",
                                tint = if (serviceEnabled) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.error
                            )
                        }
                    }
                )
            }
        },
        floatingActionButton = {
            if (!isSelectionMode) {
                FloatingActionButton(
                    onClick = { navController.navigate(Screen.AddMessage.createRoute()) }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "إضافة")
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {

            if (!serviceEnabled) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("خدمة المراقبة غير مفعلة", fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("لتفعيل المراقبة:", style = MaterialTheme.typography.bodySmall)
                        Text("1. اضغط زر التفعيل", style = MaterialTheme.typography.bodySmall)
                        Text("2. ابحث عن WhatsApp Saver", style = MaterialTheme.typography.bodySmall)
                        Text("3. فعّله", style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Power, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("تفعيل الآن")
                        }
                    }
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("المراقبة نشطة - انسخ أي رسالة في WhatsApp")
                    }
                }
            }

            if (messages.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Message,
                            null,
                            Modifier.size(72.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "لا توجد رسائل محفوظة",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "انسخ رسالة من WhatsApp لحفظها",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                        Spacer(Modifier.height(16.dp))
                        OutlinedButton(
                            onClick = { navController.navigate(Screen.AddMessage.createRoute()) }
                        ) {
                            Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("إضافة يدوية")
                        }
                    }
                }
            } else {
                LazyColumn {
                    items(messages, key = { it.id }) { msg ->
                        val isSelected = msg.id in selectedIds

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .combinedClickable(
                                    onClick = {
                                        if (isSelectionMode) {
                                            viewModel.toggleSelection(msg.id)
                                        } else {
                                            navController.navigate(Screen.MessageDetail.createRoute(msg.id))
                                        }
                                    },
                                    onLongClick = {
                                        viewModel.toggleSelection(msg.id)
                                    }
                                ),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected)
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                else
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (msg.isPinned) {
                                            Icon(
                                                Icons.Default.PushPin,
                                                null,
                                                Modifier.size(14.dp),
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                            Spacer(Modifier.width(4.dp))
                                        }
                                        Text(
                                            msg.messageText,
                                            style = MaterialTheme.typography.bodyLarge,
                                            maxLines = 3,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    if (msg.notes.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            msg.notes,
                                            style = MaterialTheme.typography.bodySmall,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        msg.category,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                if (!isSelectionMode) {
                                    Column {
                                        IconButton(
                                            onClick = { viewModel.copyToClipboard(msg.messageText) },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.ContentCopy,
                                                "نسخ",
                                                modifier = Modifier.size(18.dp),
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        IconButton(
                                            onClick = { viewModel.shareMessage(msg.messageText) },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Share,
                                                "مشاركة",
                                                modifier = Modifier.size(18.dp),
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                } else {
                                    Checkbox(
                                        checked = isSelected,
                                        onCheckedChange = { viewModel.toggleSelection(msg.id) }
                                    )
                                }
                            }
                        }
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("حذف الرسائل") },
            text = { Text("هل تريد حذف ${selectedIds.size} رسالة؟") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteSelected()
                    showDeleteDialog = false
                }) {
                    Text("حذف", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }

    if (showExportDialog) {
        ExportImportDialog(
            viewModel = viewModel,
            onDismiss = { showExportDialog = false }
        )
    }
}

@Composable
private fun SortMenuItem(
    title: String,
    order: MessageViewModel.SortOrder,
    current: MessageViewModel.SortOrder,
    viewModel: MessageViewModel,
    onClick: () -> Unit
) {
    DropdownMenuItem(
        text = {
            Text(
                title,
                fontWeight = if (order == current) FontWeight.Bold else FontWeight.Normal,
                color = if (order == current) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurface
            )
        },
        onClick = {
            viewModel.setSortOrder(order)
            onClick()
        },
        leadingIcon = {
            if (order == current) {
                Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.primary)
            }
        }
    )
}

@Composable
private fun ExportImportDialog(
    viewModel: MessageViewModel,
    onDismiss: () -> Unit
) {
    var showImport by remember { mutableStateOf(false) }
    var importText by remember { mutableStateOf("") }
    var exportResult by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (showImport) "استيراد نسخة احتياطية" else "نسخ احتياطي",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            if (showImport) {
                Column {
                    Text("الصق ملف النسخة الاحتياطية (JSON):", style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = importText,
                        onValueChange = { importText = it },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp),
                        placeholder = { Text("الصق هنا...") }
                    )
                    if (exportResult != null) {
                        Spacer(Modifier.height(8.dp))
                        Text(exportResult!!, color = MaterialTheme.colorScheme.primary)
                    }
                }
            } else {
                Column {
                    Text("ماذا تريد أن تفعل؟")
                    Spacer(Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = {
                            val path = viewModel.exportAllMessages()
                            exportResult = if (path.startsWith("لا") || path.startsWith("خطأ"))
                                path else "تم الحفظ في:\n$path"
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Save, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("حفظ في الجهاز")
                    }

                    Spacer(Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = {
                            val uri = viewModel.shareBackupFile()
                            if (uri != null) {
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "application/json"
                                    putExtra(Intent.EXTRA_STREAM, uri)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                onDismiss()
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Share, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("مشاركة الملف")
                    }

                    Spacer(Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = { showImport = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.FileUpload, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("استيراد نسخة احتياطية")
                    }

                    if (exportResult != null) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            exportResult!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (showImport) {
                TextButton(
                    onClick = {
                        if (importText.isNotBlank()) {
                            viewModel.importFromJson(importText)
                            onDismiss()
                        }
                    },
                    enabled = importText.isNotBlank()
                ) {
                    Text("استيراد")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (showImport) "رجوع" else "إغلاق")
            }
        }
    )
}
