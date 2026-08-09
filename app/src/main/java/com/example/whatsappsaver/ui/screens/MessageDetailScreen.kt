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
import com.example.whatsappsaver.ui.navigation.Screen
import com.example.whatsappsaver.ui.viewmodel.MessageViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageDetailScreen(
    navController: NavController,
    messageId: Int,
    vm: MessageViewModel
) {
    val msg by vm.getMessageById(messageId).collectAsState(initial = null)
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showShareSheet by remember { mutableStateOf(false) }

    msg?.let { m ->
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("تفاصيل") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, "رجوع")
                        }
                    },
                    actions = {
                        // تثبيت
                        IconButton(onClick = { vm.togglePin(m) }) {
                            Icon(
                                Icons.Default.PushPin, "تثبيت",
                                tint = if (m.isPinned) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onPrimary
                            )
                        }
                        // نسخ
                        IconButton(onClick = { vm.copyToClipboard(m.messageText) }) {
                            Icon(Icons.Default.ContentCopy, "نسخ")
                        }
                        // مشاركة
                        IconButton(onClick = { vm.shareMessage(m.messageText) }) {
                            Icon(Icons.Default.Share, "مشاركة")
                        }
                        // تعديل
                        IconButton(onClick = {
                            navController.navigate(Screen.AddMessage.createRoute()) {
                                launchSingleTop = true
                            }
                        }) {
                            Icon(Icons.Default.Edit, "تعديل")
                        }
                        // حذف
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, "حذف", tint = MaterialTheme.colorScheme.error)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                        actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        ) { padding ->
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ═══ التصنيف والتثبيت ═══
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AssistChip(
                        onClick = {},
                        label = { Text(m.category) },
                        leadingIcon = { Icon(Icons.Default.Label, null, Modifier.size(18.dp)) },
                        shape = RoundedCornerShape(20.dp)
                    )
                    if (m.isPinned) {
                        AssistChip(
                            onClick = {},
                            label = { Text("مثبتة") },
                            leadingIcon = { Icon(Icons.Default.PushPin, null, Modifier.size(18.dp)) },
                            shape = RoundedCornerShape(20.dp),
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }
                }

                // ═══ الرسالة ═══
                Text("الرسالة:", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                Card(
                    Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(m.messageText, Modifier.padding(16.dp))
                }

                // ═══ الملاحظات ═══
                if (m.notes.isNotBlank()) {
                    Text("الملاحظات:", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    Card(
                        Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Text(m.notes, Modifier.padding(16.dp))
                    }
                }

                Spacer(Modifier.weight(1f))

                // ═══ التاريخ ═══
                Text(
                    SimpleDateFormat("dd MMMM yyyy - HH:mm", Locale("ar")).format(Date(m.timestamp)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // ═══ حوار الحذف ═══
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("حذف الرسالة") },
                text = { Text("هل أنت متأكد من حذف هذه الرسالة؟") },
                confirmButton = {
                    TextButton(onClick = {
                        vm.deleteMessage(m)
                        showDeleteDialog = false
                        navController.popBackStack()
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

    } ?: Box(Modifier.fillMaxSize(), Alignment.Center) {
        CircularProgressIndicator()
    }
}
