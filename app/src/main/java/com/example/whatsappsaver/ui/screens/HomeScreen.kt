package com.example.whatsappsaver.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.whatsappsaver.ui.navigation.Screen
import com.example.whatsappsaver.ui.viewmodel.MessageViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController, viewModel: MessageViewModel = hiltViewModel()) {
    val messages by viewModel.filteredMessages.collectAsState()
    Scaffold(
        topBar = { TopAppBar(title = { Text("WhatsApp Saver") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate(Screen.AddMessage.createRoute()) }) {
                Icon(Icons.Default.Add, contentDescription = "إضافة")
            }
        }
    ) { padding ->
        if (messages.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("لا توجد رسائل")
            }
        } else {
            LazyColumn(modifier = Modifier.padding(padding)) {
                items(messages) { msg ->
                    Card(modifier = Modifier.fillMaxWidth().padding(8.dp), onClick = {
                        navController.navigate(Screen.MessageDetail.createRoute(msg.id))
                    }) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = msg.messageText, style = MaterialTheme.typography.bodyLarge)
                            if (msg.notes.isNotBlank()) {
                                Text(text = msg.notes, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }
    }
}
