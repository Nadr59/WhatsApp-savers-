package com.example.whatsappsaver.ui.screens

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.whatsappsaver.service.ClipboardAccessibilityService
import com.example.whatsappsaver.ui.navigation.Screen
import com.example.whatsappsaver.ui.viewmodel.MessageViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController, viewModel: MessageViewModel = hiltViewModel()) {
    val messages by viewModel.filteredMessages.collectAsState()
    val context = LocalContext.current
    var serviceEnabled by remember { mutableStateOf(ClipboardAccessibilityService.isRunning()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("WhatsApp Saver") },
                actions = {
                    // زر تفعيل/تعطيل مراقبة الحافظة
                    IconButton(onClick = {
                        if (!serviceEnabled) {
                            // فتح إعدادات Accessibility
                            context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                        }
                    }) {
                        Icon(
                            if (serviceEnabled) Icons.Default.Notifications
                            else Icons.Default.NotificationsOff,
                            contentDescription = "مراقبة",
                            tint = if (serviceEnabled)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.error
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate(Screen.AddMessage.createRoute()) }) {
                Icon(Icons.Default.Add, contentDescription = "إضافة")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // بانر تفعيل الخدمة
            if (!serviceEnabled) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "خدمة المراقبة غير مفعلة",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                "اضغط لتفعيل مراقبة رسائل WhatsApp",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        TextButton(onClick = {
                            context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                        }) {
                            Text("تفعيل")
                        }
                    }
                }
            }

            // قائمة الرسائل
            if (messages.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Message,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("لا توجد رسائل محفوظة")
                        Text(
                            "انسخ رسالة من WhatsApp لحفظها",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            } else {
                LazyColumn {
                    items(messages) { msg ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            onClick = {
                                navController.navigate(Screen.MessageDetail.createRoute(msg.id))
                            }
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                if (msg.isPinned) {
                                    Icon(
                                        Icons.Default.PushPin,
                                        contentDescription = "مثبتة",
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Text(
                                    text = msg.messageText,
                                    style = MaterialTheme.typography.bodyLarge,
                                    maxLines = 3
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = msg.category,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
