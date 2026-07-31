package com.example.whatsappsaver.ui.navigation
sealed class Screen(val route: String) {
    object Home : Screen("home")
    object AddMessage : Screen("add_message?sharedText={sharedText}") { fun createRoute(t: String = "") = "add_message?sharedText=$t" }
    object MessageDetail : Screen("message_detail/{messageId}") { fun createRoute(id: Int) = "message_detail/$id" }
    object Categories : Screen("categories")
    object Search : Screen("search")
}
