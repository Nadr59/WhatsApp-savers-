package com.example.whatsappsaver.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object AddMessage : Screen("add_message") {
        fun createRoute(): String = "add_message"
    }
    object MessageDetail : Screen("message_detail/{messageId}") {
        fun createRoute(messageId: Int): String = "message_detail/$messageId"
    }
    object Categories : Screen("categories")
    object Search : Screen("search")
}

sealed class BottomNavItem(val route: String, val title: String, val icon: ImageVector) {
    object Home : BottomNavItem("home", "الرئيسية", Icons.Default.Home)
    object Categories : BottomNavItem("categories", "التصنيفات", Icons.Default.Folder)
    object Search : BottomNavItem("search", "البحث", Icons.Default.Search)
}
