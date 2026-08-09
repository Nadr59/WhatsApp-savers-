package com.example.whatsappsaver.ui.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")

    object AddMessage : Screen("add_message/{editId}") {
        fun createRoute(editId: Int = -1): String = "add_message/$editId"
    }

    object MessageDetail : Screen("message_detail/{messageId}") {
        fun createRoute(messageId: Int): String = "message_detail/$messageId"
    }

    object Categories : Screen("categories")
    object Search : Screen("search")
}
