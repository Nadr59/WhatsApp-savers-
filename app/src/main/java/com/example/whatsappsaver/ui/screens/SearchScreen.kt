package com.example.whatsappsaver.ui.screens
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.whatsappsaver.ui.navigation.Screen
import com.example.whatsappsaver.ui.viewmodel.MessageViewModel
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(navController: NavController, vm: MessageViewModel = hiltViewModel()) {
    val q by vm.searchQuery.collectAsState()
    val msgs by vm.filteredMessages.collectAsState()
    Scaffold(topBar = { TopAppBar(title = { OutlinedTextField(q, { vm.setSearchQuery(it) }, placeholder = { Text("ابحث...") }, singleLine = true, modifier = Modifier.fillMaxWidth(), leadingIcon = { Icon(Icons.Default.Search, null) }) }, colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)) }) { p ->
        Column(Modifier.fillMaxSize().padding(p)) {
            if (q.isBlank()) Box(Modifier.fillMaxSize(), Alignment.Center) { Text("اكتب كلمة للبحث") }
            else if (msgs.isEmpty()) Box(Modifier.fillMaxSize(), Alignment.Center) { Text("لا نتائج") }
            else LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { items(msgs, key = { it.id }) { m -> Card(onClick = { navController.navigate(Screen.MessageDetail.createRoute(m.id)) }, Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp)) { Text(m.messageText, maxLines = 3, overflow = TextOverflow.Ellipsis); Spacer(Modifier.height(8.dp)); Text(m.category, color = MaterialTheme.colorScheme.primary) } } } }
        }
    }
}
