package com.example.whatsappsaver
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sharedText = when (intent?.action) { Intent.ACTION_SEND -> if (intent.type == "text/plain") intent.getStringExtra(Intent.EXTRA_TEXT) ?: "" else ""; else -> "" }
        setContent { WhatsAppSaverTheme { WhatsAppSaverApp(sharedText = sharedText) } }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WhatsAppSaverApp(sharedText: String = "") {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val bottomItems = listOf(BottomNavItem.Home, BottomNavItem.Categories, BottomNavItem.Search)
    Scaffold(bottomBar = { if (currentDestination?.route in bottomItems.map { it.route }) { NavigationBar { bottomItems.forEach { item -> NavigationBarItem(icon = { Icon(item.icon, contentDescription = item.title) }, label = { Text(item.title) }, selected = currentDestination?.hierarchy?.any { it.route == item.route } == true, onClick = { navController.navigate(item.route) { popUpTo(navController.graph.findStartDestination().id) { saveState = true }; launchSingleTop = true; restoreState = true } }) } } } }) { innerPadding ->
        NavHost(navController = navController, startDestination = Screen.Home.route, modifier = Modifier.padding(innerPadding)) {
            composable(Screen.Home.route) { HomeScreen(navController = navController) }
            composable(Screen.AddMessage.route, arguments = listOf(navArgument("sharedText") { type = NavType.StringType; defaultValue = "" })) { be -> AddMessageScreen(navController = navController, sharedText = URLDecoder.decode(be.arguments?.getString("sharedText") ?: "", StandardCharsets.UTF_8.toString())) }
            composable(Screen.MessageDetail.route, arguments = listOf(navArgument("messageId") { type = NavType.IntType })) { be -> MessageDetailScreen(navController = navController, messageId = be.arguments?.getInt("messageId") ?: 0) }
            composable(Screen.Categories.route) { CategoriesScreen(navController = navController) }
            composable(Screen.Search.route) { SearchScreen(navController = navController) }
        }
    }
}
