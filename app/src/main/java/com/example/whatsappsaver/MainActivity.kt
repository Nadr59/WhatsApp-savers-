package com.example.whatsappsaver

import android.Manifest
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.whatsappsaver.ui.navigation.BottomNavItem
import com.example.whatsappsaver.ui.navigation.Screen
import com.example.whatsappsaver.ui.screens.*
import com.example.whatsappsaver.ui.theme.WhatsAppSaverTheme
import dagger.hilt.android.AndroidEntryPoint
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // نص الحافظة اللي نقرأه لما التطبيق يفتح
    private var clipboardText = mutableStateOf("")

    private val notifPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // طلب إذن الإشعارات (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                notifPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // قراءة الحافظة إذا جينا من إشعار
        handleIntent(intent)

        setContent {
            WhatsAppSaverTheme {
                val sharedText by remember { mutableStateOf("") }

                // إذا فيه نص من الحافظة
                val clipText = clipboardText.value

                WhatsAppSaverApp(
                    sharedText = clipText.ifBlank { sharedText }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        when (intent?.action) {
            "SAVE_FROM_CLIPBOARD" -> {
                // التطبيق فتح من إشعار - اقرأ الحافظة
                readClipboardAndSave()
            }
            Intent.ACTION_SEND -> {
                // مشاركة من WhatsApp
                val text = intent.getStringExtra(Intent.EXTRA_TEXT) ?: ""
                if (text.isNotBlank()) {
                    clipboardText.value = text
                }
            }
        }
    }

    private fun readClipboardAndSave() {
        val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = cm.primaryClip
        if (clip != null && clip.itemCount > 0) {
            val text = clip.getItemAt(0).text?.toString()
            if (!text.isNullOrBlank()) {
                clipboardText.value = text
            } else {
                Toast.makeText(this, "الحافظة فارغة", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "لا يوجد نص في الحافظة", Toast.LENGTH_SHORT).show()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WhatsAppSaverApp(sharedText: String = "") {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val bottomItems = listOf(BottomNavItem.Home, BottomNavItem.Categories, BottomNavItem.Search)

    // إذا فيه نص → افتح شاشة الإضافة
    LaunchedEffect(sharedText) {
        if (sharedText.isNotBlank()) {
            navController.navigate(Screen.AddMessage.createRoute(sharedText))
        }
    }

    Scaffold(
        bottomBar = {
            if (currentDestination?.route in bottomItems.map { it.route }) {
                NavigationBar {
                    bottomItems.forEach { item ->
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.title) },
                            label = { Text(item.title) },
                            selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(navController = navController)
            }
            composable(
                Screen.AddMessage.route,
                arguments = listOf(navArgument("sharedText") {
                    type = NavType.StringType
                    defaultValue = ""
                })
            ) { entry ->
                AddMessageScreen(
                    navController = navController,
                    sharedText = URLDecoder.decode(
                        entry.arguments?.getString("sharedText") ?: "",
                        StandardCharsets.UTF_8.toString()
                    )
                )
            }
            composable(
                Screen.MessageDetail.route,
                arguments = listOf(navArgument("messageId") { type = NavType.IntType })
            ) { entry ->
                MessageDetailScreen(
                    navController = navController,
                    messageId = entry.arguments?.getInt("messageId") ?: 0
                )
            }
            composable(Screen.Categories.route) {
                CategoriesScreen(navController = navController)
            }
            composable(Screen.Search.route) {
                SearchScreen(navController = navController)
            }
        }
    }
}
