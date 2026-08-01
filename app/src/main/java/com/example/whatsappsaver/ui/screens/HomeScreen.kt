package com.example.whatsappsaver.ui.screens

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
import com.example.whatsappsaver.service.WhatsAppNotificationListener
import com.example.whatsappsaver.ui.navigation.Screen
import com.example.whatsappsaver.ui.viewmodel.MessageViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController, viewModel: MessageViewModel = hiltViewModel()) {
    val messages by viewModel.filteredMessages.collectAsState()
    val context = LocalContext.current

    fun isListenerEnabled(): Boolean {
        val flat = Settings.Secure.getString(
            context.contentResolver,
            "enabled_notification_listeners"
        ) ?: return false
        return flat.contains(
            context.packageName + "/" + WhatsAppNotificationListener::class.java.canonicalName,
            ignoreCase = true
        )
    }

    var serviceEnabled by remember { mutableStateOf(isListenerEnabled()) }

    // إعادة التحقق عند العودة للشاشة
    LaunchedEffect(Unit) {
        serviceEnabled = isListenerEnabled()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("WhatsApp Saver") },
                actions = {
                    IconButton(onClick = {
                        serviceEnabled = isListenerEnabled()
                        if (!serviceEnabled) {
                            context.startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))
                        }
                    }) {
                        Icon(
                            imageVector = if (serviceEnabled) Icons.Default.Notifications
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

            // بانر التفعيل
            if (!serviceEnabled) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("خدمة المراقبة غير مفعلة")
                                Text(
                                    "اضغط تفعيل لإعطاء إذن قراءة الإشعارات",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "خطوات التفعيل:",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            "1. اضغط زر تفعيل\n2. ابحث عن WhatsApp Saver\n3. فعّله",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                context.startActivity(
                                    Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("تفعيل الآن")
                        }
                    }
                }
            } else {
                // رسالة تأكيد الخدمة
                Card(
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("المراقبة نشطة - أي رسالة وصلك في WhatsApp راح تظهر هنا")
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
