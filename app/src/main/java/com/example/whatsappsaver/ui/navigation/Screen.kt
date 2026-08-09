package com.example.whatsappsaver.ui.navigation

import android.net.Uri

sealed class Screen(val route: String) {
    object Home : Screen("home")

    object AddMessage : Screen("add_message/{editId}/{editText}/{editCategory}/{editNotes}") {
        fun createRoute(
            editId: Int = -1,
            editText: String = "",
            editCategory: String = "عام",
            editNotes: String = ""
        ): String {
            return "add_message/$editId/${Uri.encode(editText)}/${Uri.encode(editCategory)}/${Uri.encode(editNotes)}"
        }
    }

    object MessageDetail : Screen("message_detail/{messageId}") {
        fun createRoute(messageId: Int): String = "message_detail/$messageId"
    }

    object Categories : Screen("categories")
    object Search : Screen("search")
}
