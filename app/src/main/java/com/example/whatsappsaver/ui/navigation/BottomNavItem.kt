package com.example.whatsappsaver.ui.navigation
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
sealed class BottomNavItem(val route: String, val title: String, val icon: ImageVector) {
    object Home : BottomNavItem(Screen.Home.route, "الرئيسية", Icons.Default.Home)
    object Categories : BottomNavItem(Screen.Categories.route, "التصنيفات", Icons.Default.Folder)
    object Search : BottomNavItem(Screen.Search.route, "البحث", Icons.Default.Search)
}
