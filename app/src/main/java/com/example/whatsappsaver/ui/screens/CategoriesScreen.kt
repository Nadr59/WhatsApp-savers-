package com.example.whatsappsaver.ui.screens
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
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
fun CategoriesScreen(navController: NavController, vm: MessageViewModel = hiltViewModel()) {
    val cats by vm.categories.collectAsState()
    val msgs by vm.allMessages.collectAsState()
    Scaffold(topBar = { TopAppBar(title = { Text("التصنيفات") }, colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary, titleContentColor = MaterialTheme.colorScheme.onPrimary)) }) { p ->
        LazyColumn(Modifier.fillMaxSize().padding(p).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item { Text("التصنيفات", style = MaterialTheme.typography.titleLarge) }
            items(cats) { c -> Card(onClick = { vm.setCategory(c); navController.navigate("home") { popUpTo("home") { inclusive = true } } }, Modifier.fillMaxWidth()) { Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Folder, null, tint = MaterialTheme.colorScheme.primary); Spacer(Modifier.width(12.dp)); Text(c) }; Badge { Text("${msgs.count { it.category == c }}") } } } }
        }
    }
}
