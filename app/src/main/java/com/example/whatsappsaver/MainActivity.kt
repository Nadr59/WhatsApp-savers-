package com.example.whatsappsaver

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
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
import com.example.whatsappsaver.service.ClipboardMonitorService
import com.example.whatsappsaver.ui.navigation.BottomNavItem
import com.example.whatsappsaver.ui.navigation.Screen
import com.example.whatsappsaver.ui.screens.*
import com.example.whatsappsaver.ui.theme.WhatsAppSaverTheme
import dagger.hilt.android.AndroidEntryPoint
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private var monitorServiceRunning = false

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) startClipboardService()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // طلب إذن الإشعارات (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // تشغيل الخدمة تلقائياً
        startClipboardService()

        val sharedText = extractSharedText(intent)

        setContent {
            WhatsAppSaverTheme {
                WhatsAppSaverApp(
                    sharedText = sharedText,
                    isMonitorRunning = monitorServiceRunning,
                    onToggleMonitor = { toggleMonitor() }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val text = extractSharedText(intent)
        if (text.isNotBlank()) {
            // سيتم التعامل معه عبر recomposition
        }
    }

    private fun extractSharedText(intent: Intent?): String {
        return when (intent?.action) {
            Intent.ACTION_SEND -> {
                if (intent.type == "text/plain")
                    intent.getStringExtra(Intent.EXTRA_TEXT) ?: ""
                else ""
            }
            ClipboardMonitorService.ACTION_SAVE -> {
                intent.getStringExtra(ClipboardMonitorService.EXTRA_TEXT) ?: ""
            }
            else -> ""
        }
    }

    private fun startClipboardService() {
        val intent = Intent(this, ClipboardMonitorService::class.java)
        ContextCompat.startForegroundService(this, intent)
        monitorServiceRunning = true
    }

    private fun stopClipboardService() {
        val intent = Intent(this, ClipboardMonitorService::class.java)
        stopService(intent)
        monitorServiceRunning = false
    }

    private fun toggleMonitor() {
        if (monitorServiceRunning) stopClipboardService() else startClipboardService()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WhatsAppSaverApp(
    sharedText: String = "",
    isMonitorRunning: Boolean = true,
    onToggleMonitor: () -> Unit = {}
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val bottomItems = listOf(BottomNavItem.Home, BottomNavItem.Categories, BottomNavItem.Search)

    // إذا كان فيه رسالة مشاركة → افتح شاشة الإضافة
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
            ) { backStackEntry ->
                AddMessageScreen(
                    navController = navController,
                    sharedText = URLDecoder.decode(
                        backStackEntry.arguments?.getString("sharedText") ?: "",
                        StandardCharsets.UTF_8.toString()
                    )
                )
            }
            composable(
                Screen.MessageDetail.route,
                arguments = listOf(navArgument("messageId") { type = NavType.IntType })
            ) { backStackEntry ->
                MessageDetailScreen(
                    navController = navController,
                    messageId = backStackEntry.arguments?.getInt("messageId") ?: 0
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
