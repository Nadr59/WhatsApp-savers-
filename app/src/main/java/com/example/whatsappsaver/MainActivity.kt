package com.example.whatsappsaver

import android.Manifest
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    companion object {
        // النص يُمرر هنا بدال URL
        var clipboardText: String = ""
    }

    // إشارة للتنقل (عدّاد يتغير)
    private val _navSignal = mutableStateOf(0)
    private val handler = Handler(Looper.getMainLooper())

    private val notifPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                notifPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        handleIntent(intent)

        setContent {
            WhatsAppSaverTheme {
                val signal by _navSignal
                MainApp(
                    navSignal = signal,
                    onNavConsumed = { _navSignal.value = 0 }
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
                handler.postDelayed({ readClipboard() }, 600)
            }
            Intent.ACTION_SEND -> {
                val text = intent.getStringExtra(Intent.EXTRA_TEXT) ?: ""
                if (text.isNotBlank()) {
                    clipboardText = text
                    _navSignal.value++
                }
            }
        }
    }

    private fun readClipboard() {
        try {
            val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = cm.primaryClip
            if (clip != null && clip.itemCount > 0) {
                val text = clip.getItemAt(0).text?.toString()
                if (!text.isNullOrBlank()) {
                    clipboardText = text
                    _navSignal.value++
                    Toast.makeText(this, "تم جلب الرسالة!", Toast.LENGTH_SHORT).show()
                    return
                }
            }
            Toast.makeText(this, "لا شيء في الحافظة", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "خطأ: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp(navSignal: Int, onNavConsumed: () -> Unit) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val bottomItems = listOf(BottomNavItem.Home, BottomNavItem.Categories, BottomNavItem.Search)

    // لما تتغير الإشارة → اقرأ النص وانتقل
    LaunchedEffect(navSignal) {
        if (navSignal > 0) {
            onNavConsumed()
            val text = MainActivity.clipboardText
            if (text.isNotBlank()) {
                MainActivity.clipboardText = ""
                navController.navigate(Screen.AddMessage.createRoute()) {
                    launchSingleTop = true
                }
            }
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
            composable(Screen.AddMessage.route) {
                // اقرأ النص من companion object
                val text = remember {
                    val t = MainActivity.clipboardText
                    MainActivity.clipboardText = ""
                    t
                }
                AddMessageScreen(
                    navController = navController,
                    sharedText = text
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
