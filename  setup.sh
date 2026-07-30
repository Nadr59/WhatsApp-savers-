#!/bin/bash

# ============================================================
# WhatsApp Saver - Project Setup & Push to GitHub
# Repository: https://github.com/Nadr59/WhatsApp-savers-
# ============================================================

set -e

REPO_URL="https://github.com/Nadr59/WhatsApp-savers-.git"
PROJECT_DIR="WhatsAppSaver"

echo "=========================================="
echo "  WhatsApp Saver - Project Setup Script"
echo "=========================================="

# Check if git is installed
if ! command -v git &> /dev/null; then
    echo "❌ Git is not installed. Please install git first."
    exit 1
fi

# Remove old directory if exists
if [ -d "$PROJECT_DIR" ]; then
    echo "⚠️  Removing existing $PROJECT_DIR directory..."
    rm -rf "$PROJECT_DIR"
fi

echo "📁 Creating project structure..."

# ============================================================
# CREATE DIRECTORY STRUCTURE
# ============================================================

mkdir -p "$PROJECT_DIR/app/src/main/java/com/example/whatsappsaver/data/local/dao"
mkdir -p "$PROJECT_DIR/app/src/main/java/com/example/whatsappsaver/data/local/entity"
mkdir -p "$PROJECT_DIR/app/src/main/java/com/example/whatsappsaver/data/repository"
mkdir -p "$PROJECT_DIR/app/src/main/java/com/example/whatsappsaver/di"
mkdir -p "$PROJECT_DIR/app/src/main/java/com/example/whatsappsaver/ui/navigation"
mkdir -p "$PROJECT_DIR/app/src/main/java/com/example/whatsappsaver/ui/screens"
mkdir -p "$PROJECT_DIR/app/src/main/java/com/example/whatsappsaver/ui/theme"
mkdir -p "$PROJECT_DIR/app/src/main/java/com/example/whatsappsaver/ui/viewmodel"
mkdir -p "$PROJECT_DIR/app/src/main/res/values"
mkdir -p "$PROJECT_DIR/app/src/main/res/values-ar"
mkdir -p "$PROJECT_DIR/app/src/main/res/xml"
mkdir -p "$PROJECT_DIR/app/src/main/res/layout"
mkdir -p "$PROJECT_DIR/app/src/main/res/drawable"
mkdir -p "$PROJECT_DIR/app/src/main/res/mipmap-hdpi"
mkdir -p "$PROJECT_DIR/gradle/wrapper"
mkdir -p "$PROJECT_DIR/.github/workflows"

echo "✅ Directory structure created."

# ============================================================
# ROOT PROJECT FILES
# ============================================================

echo "📝 Creating root project files..."

# --- settings.gradle ---
cat > "$PROJECT_DIR/settings.gradle" << 'ENDOFFILE'
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}
rootProject.name = "WhatsAppSaver"
include ':app'
ENDOFFILE

# --- build.gradle (root) ---
cat > "$PROJECT_DIR/build.gradle" << 'ENDOFFILE'
// Top-level build file
buildscript {
    ext {
        compose_version = '1.6.8'
        room_version = '2.6.1'
        hilt_version = '2.51'
        kotlin_version = '1.9.23'
    }
    dependencies {
        classpath "com.android.tools.build:gradle:8.4.0"
        classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin_version"
        classpath "com.google.dagger:hilt-android-gradle-plugin:$hilt_version"
    }
}

plugins {
    id 'com.android.application' version '8.4.0' apply false
    id 'com.android.library' version '8.4.0' apply false
    id 'org.jetbrains.kotlin.android' version '1.9.23' apply false
    id 'com.google.dagger.hilt.android' version '2.51' apply false
}
ENDOFFILE

# --- gradle.properties ---
cat > "$PROJECT_DIR/gradle.properties" << 'ENDOFFILE'
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
android.useAndroidX=true
kotlin.code.style=official
android.nonTransitiveRClass=true
ENDOFFILE

# --- gradle-wrapper.properties ---
cat > "$PROJECT_DIR/gradle/wrapper/gradle-wrapper.properties" << 'ENDOFFILE'
distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
distributionUrl=https\://services.gradle.org/distributions/gradle-8.6-bin.zip
zipStoreBase=GRADLE_USER_HOME
zipStorePath=wrapper/dists
ENDOFFILE

# --- .gitignore ---
cat > "$PROJECT_DIR/.gitignore" << 'ENDOFFILE'
*.iml
.gradle
/local.properties
/.idea
.DS_Store
/build
/captures
.externalNativeBuild
.cxx
local.properties
/app/build
*.apk
*.ap_
*.aab
ENDOFFILE

echo "✅ Root project files created."

# ============================================================
# APP MODULE FILES
# ============================================================

echo "📝 Creating app module files..."

# --- app/build.gradle ---
cat > "$PROJECT_DIR/app/build.gradle" << 'ENDOFFILE'
plugins {
    id 'com.android.application'
    id 'org.jetbrains.kotlin.android'
    id 'kotlin-kapt'
    id 'com.google.dagger.hilt.android'
}

android {
    namespace 'com.example.whatsappsaver'
    compileSdk 34

    defaultConfig {
        applicationId "com.example.whatsappsaver"
        minSdk 24
        targetSdk 34
        versionCode 1
        versionName "1.0"

        testInstrumentationRunner "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary true
        }
    }

    buildTypes {
        release {
            minifyEnabled false
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
    }
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_17
        targetCompatibility JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = '17'
    }
    buildFeatures {
        compose true
    }
    composeOptions {
        kotlinCompilerExtensionVersion '1.5.13'
    }
    packaging {
        resources {
            excludes += '/META-INF/{AL2.0,LGPL2.1}'
        }
    }
}

dependencies {
    // Core
    implementation 'androidx.core:core-ktx:1.13.1'
    implementation 'androidx.lifecycle:lifecycle-runtime-ktx:2.8.2'
    implementation 'androidx.activity:activity-compose:1.9.0'

    // Compose
    implementation platform('androidx.compose:compose-bom:2024.05.00')
    implementation 'androidx.compose.ui:ui'
    implementation 'androidx.compose.ui:ui-graphics'
    implementation 'androidx.compose.ui:ui-tooling-preview'
    implementation 'androidx.compose.material3:material3'
    implementation 'androidx.compose.material:material-icons-extended'

    // Navigation
    implementation 'androidx.navigation:navigation-compose:2.7.7'
    implementation 'androidx.hilt:hilt-navigation-compose:1.2.0'

    // ViewModel
    implementation 'androidx.lifecycle:lifecycle-viewmodel-compose:2.8.2'
    implementation 'androidx.lifecycle:lifecycle-runtime-compose:2.8.2'

    // Room
    implementation "androidx.room:room-runtime:$room_version"
    implementation "androidx.room:room-ktx:$room_version"
    kapt "androidx.room:room-compiler:$room_version"

    // Hilt
    implementation "com.google.dagger:hilt-android:$hilt_version"
    kapt "com.google.dagger:hilt-compiler:$hilt_version"

    // Testing
    testImplementation 'junit:junit:4.13.2'
    androidTestImplementation 'androidx.test.ext:junit:1.1.5'
    androidTestImplementation 'androidx.test.espresso:espresso-core:3.5.1'
    androidTestImplementation platform('androidx.compose:compose-bom:2024.05.00')
    androidTestImplementation 'androidx.compose.ui:ui-test-junit4'
    debugImplementation 'androidx.compose.ui:ui-tooling'
    debugImplementation 'androidx.compose.ui:ui-test-manifest'
}
ENDOFFILE

# --- app/proguard-rules.pro ---
cat > "$PROJECT_DIR/app/proguard-rules.pro" << 'ENDOFFILE'
# WhatsApp Saver ProGuard Rules

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Hilt
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.lifecycle.HiltViewModel

# Compose
-dontwarn androidx.compose.**

# Keep data classes
-keep class com.example.whatsappsaver.data.local.entity.** { *; }
ENDOFFILE

echo "✅ App module files created."

# ============================================================
# ANDROID MANIFEST
# ============================================================

echo "📝 Creating AndroidManifest.xml..."

cat > "$PROJECT_DIR/app/src/main/AndroidManifest.xml" << 'ENDOFFILE'
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">

    <application
        android:name=".WhatsAppSaverApp"
        android:allowBackup="true"
        android:dataExtractionRules="@xml/data_extraction_rules"
        android:fullBackupContent="@xml/backup_rules"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.WhatsAppSaver"
        tools:targetApi="31">

        <!-- Main Activity -->
        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:theme="@style/Theme.WhatsAppSaver"
            android:windowSoftInputMode="adjustResize">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>

            <!-- Share Intent Filter - Receive text from WhatsApp and other apps -->
            <intent-filter>
                <action android:name="android.intent.action.SEND" />
                <category android:name="android.intent.category.DEFAULT" />
                <data android:mimeType="text/plain" />
            </intent-filter>
        </activity>
    </application>

</manifest>
ENDOFFILE

echo "✅ AndroidManifest.xml created."

# ============================================================
# RESOURCE FILES
# ============================================================

echo "📝 Creating resource files..."

# --- values/strings.xml ---
cat > "$PROJECT_DIR/app/src/main/res/values/strings.xml" << 'ENDOFFILE'
<resources>
    <string name="app_name">WhatsApp Saver</string>
</resources>
ENDOFFILE

# --- values-ar/strings.xml ---
cat > "$PROJECT_DIR/app/src/main/res/values-ar/strings.xml" << 'ENDOFFILE'
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="app_name">حفظ رسائل واتساب</string>
</resources>
ENDOFFILE

# --- values/themes.xml ---
cat > "$PROJECT_DIR/app/src/main/res/values/themes.xml" << 'ENDOFFILE'
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <style name="Theme.WhatsAppSaver" parent="android:Theme.Material.Light.NoActionBar">
        <item name="android:statusBarColor">@android:color/transparent</item>
        <item name="android:navigationBarColor">@android:color/transparent</item>
    </style>
</resources>
ENDOFFILE

# --- xml/backup_rules.xml ---
cat > "$PROJECT_DIR/app/src/main/res/xml/backup_rules.xml" << 'ENDOFFILE'
<?xml version="1.0" encoding="utf-8"?>
<full-backup-content>
    <include domain="sharedpref" path="." />
    <exclude domain="no_backup" path="." />
    <include domain="database" path="." />
</full-backup-content>
ENDOFFILE

# --- xml/data_extraction_rules.xml ---
cat > "$PROJECT_DIR/app/src/main/res/xml/data_extraction_rules.xml" << 'ENDOFFILE'
<?xml version="1.0" encoding="utf-8"?>
<data-extraction-rules>
    <cloud-backup>
        <include domain="sharedpref" path="." />
        <include domain="database" path="." />
    </cloud-backup>
    <device-transfer>
        <include domain="sharedpref" path="." />
        <include domain="database" path="." />
    </device-transfer>
</data-extraction-rules>
ENDOFFILE

echo "✅ Resource files created."

# ============================================================
# KOTLIN SOURCE FILES
# ============================================================

echo "📝 Creating Kotlin source files..."

# --- WhatsAppSaverApp.kt ---
cat > "$PROJECT_DIR/app/src/main/java/com/example/whatsappsaver/WhatsAppSaverApp.kt" << 'ENDOFFILE'
package com.example.whatsappsaver

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class WhatsAppSaverApp : Application()
ENDOFFILE

# --- MainActivity.kt ---
cat > "$PROJECT_DIR/app/src/main/java/com/example/whatsappsaver/MainActivity.kt" << 'ENDOFFILE'
package com.example.whatsappsaver

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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

        // Handle shared text from WhatsApp
        val sharedText = when (intent?.action) {
            Intent.ACTION_SEND -> {
                if (intent.type == "text/plain") {
                    intent.getStringExtra(Intent.EXTRA_TEXT) ?: ""
                } else ""
            }
            else -> ""
        }

        setContent {
            WhatsAppSaverTheme {
                WhatsAppSaverApp(sharedText = sharedText)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WhatsAppSaverApp(sharedText: String = "") {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val bottomItems = listOf(
        BottomNavItem.Home,
        BottomNavItem.Categories,
        BottomNavItem.Search
    )

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
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
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
                route = Screen.AddMessage.route,
                arguments = listOf(
                    navArgument("sharedText") {
                        type = NavType.StringType
                        defaultValue = ""
                    }
                )
            ) { backStackEntry ->
                val text = backStackEntry.arguments?.getString("sharedText") ?: ""
                val decodedText = URLDecoder.decode(text, StandardCharsets.UTF_8.toString())
                AddMessageScreen(
                    navController = navController,
                    sharedText = decodedText
                )
            }
            composable(
                route = Screen.MessageDetail.route,
                arguments = listOf(navArgument("messageId") { type = NavType.IntType })
            ) { backStackEntry ->
                val messageId = backStackEntry.arguments?.getInt("messageId") ?: 0
                MessageDetailScreen(
                    navController = navController,
                    messageId = messageId
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
ENDOFFILE

# --- data/local/AppDatabase.kt ---
cat > "$PROJECT_DIR/app/src/main/java/com/example/whatsappsaver/data/local/AppDatabase.kt" << 'ENDOFFILE'
package com.example.whatsappsaver.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.whatsappsaver.data.local.dao.MessageDao
import com.example.whatsappsaver.data.local.entity.Message

@Database(
    entities = [Message::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun messageDao(): MessageDao
}
ENDOFFILE

# --- data/local/dao/MessageDao.kt ---
cat > "$PROJECT_DIR/app/src/main/java/com/example/whatsappsaver/data/local/dao/MessageDao.kt" << 'ENDOFFILE'
package com.example.whatsappsaver.data.local.dao

import androidx.room.*
import com.example.whatsappsaver.data.local.entity.Message
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {

    @Query("SELECT * FROM messages ORDER BY isPinned DESC, timestamp DESC")
    fun getAllMessages(): Flow<List<Message>>

    @Query("SELECT * FROM messages WHERE id = :id")
    suspend fun getMessageById(id: Int): Message?

    @Query("SELECT * FROM messages WHERE messageText LIKE '%' || :query || '%' OR notes LIKE '%' || :query || '%' ORDER BY isPinned DESC, timestamp DESC")
    fun searchMessages(query: String): Flow<List<Message>>

    @Query("SELECT * FROM messages WHERE category = :category ORDER BY isPinned DESC, timestamp DESC")
    fun getMessagesByCategory(category: String): Flow<List<Message>>

    @Query("SELECT DISTINCT category FROM messages")
    fun getAllCategories(): Flow<List<String>>

    @Query("SELECT * FROM messages WHERE timestamp >= :startOfDay AND timestamp < :endOfDay ORDER BY timestamp DESC")
    fun getMessagesByDay(startOfDay: Long, endOfDay: Long): Flow<List<Message>>

    @Query("SELECT * FROM messages WHERE timestamp >= :startOfMonth AND timestamp < :endOfMonth ORDER BY timestamp DESC")
    fun getMessagesByMonth(startOfMonth: Long, endOfMonth: Long): Flow<List<Message>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: Message): Long

    @Update
    suspend fun updateMessage(message: Message)

    @Delete
    suspend fun deleteMessage(message: Message)

    @Query("UPDATE messages SET isPinned = :isPinned WHERE id = :id")
    suspend fun updatePinStatus(id: Int, isPinned: Boolean)
}
ENDOFFILE

# --- data/local/entity/Message.kt ---
cat > "$PROJECT_DIR/app/src/main/java/com/example/whatsappsaver/data/local/entity/Message.kt" << 'ENDOFFILE'
package com.example.whatsappsaver.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class Message(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val messageText: String,
    val notes: String = "",
    val category: String = "عام", // Default category
    val timestamp: Long = System.currentTimeMillis(),
    val isPinned: Boolean = false
)
ENDOFFILE

# --- data/repository/MessageRepository.kt ---
cat > "$PROJECT_DIR/app/src/main/java/com/example/whatsappsaver/data/repository/MessageRepository.kt" << 'ENDOFFILE'
package com.example.whatsappsaver.data.repository

import com.example.whatsappsaver.data.local.dao.MessageDao
import com.example.whatsappsaver.data.local.entity.Message
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MessageRepository @Inject constructor(
    private val messageDao: MessageDao
) {
    fun getAllMessages(): Flow<List<Message>> = messageDao.getAllMessages()

    fun searchMessages(query: String): Flow<List<Message>> = messageDao.searchMessages(query)

    fun getMessagesByCategory(category: String): Flow<List<Message>> =
        messageDao.getMessagesByCategory(category)

    fun getAllCategories(): Flow<List<String>> = messageDao.getAllCategories()

    suspend fun getMessageById(id: Int): Message? = messageDao.getMessageById(id)

    suspend fun insertMessage(message: Message): Long = messageDao.insertMessage(message)

    suspend fun updateMessage(message: Message) = messageDao.updateMessage(message)

    suspend fun deleteMessage(message: Message) = messageDao.deleteMessage(message)

    suspend fun togglePinStatus(message: Message) {
        messageDao.updatePinStatus(message.id, !message.isPinned)
    }
}
ENDOFFILE

# --- di/AppModule.kt ---
cat > "$PROJECT_DIR/app/src/main/java/com/example/whatsappsaver/di/AppModule.kt" << 'ENDOFFILE'
package com.example.whatsappsaver.di

import android.content.Context
import androidx.room.Room
import com.example.whatsappsaver.data.local.AppDatabase
import com.example.whatsappsaver.data.local.dao.MessageDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "whatsapp_saver_db"
        ).build()
    }

    @Provides
    fun provideMessageDao(database: AppDatabase): MessageDao {
        return database.messageDao()
    }
}
ENDOFFILE

# --- ui/navigation/Screen.kt ---
cat > "$PROJECT_DIR/app/src/main/java/com/example/whatsappsaver/ui/navigation/Screen.kt" << 'ENDOFFILE'
package com.example.whatsappsaver.ui.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object AddMessage : Screen("add_message?sharedText={sharedText}") {
        fun createRoute(sharedText: String = ""): String {
            return "add_message?sharedText=$sharedText"
        }
    }
    object MessageDetail : Screen("message_detail/{messageId}") {
        fun createRoute(messageId: Int): String {
            return "message_detail/$messageId"
        }
    }
    object Categories : Screen("categories")
    object Search : Screen("search")
}
ENDOFFILE

# --- ui/navigation/BottomNavItem.kt ---
cat > "$PROJECT_DIR/app/src/main/java/com/example/whatsappsaver/ui/navigation/BottomNavItem.kt" << 'ENDOFFILE'
package com.example.whatsappsaver.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Home : BottomNavItem(Screen.Home.route, "الرئيسية", Icons.Default.Home)
    object Categories : BottomNavItem(Screen.Categories.route, "التصنيفات", Icons.Default.Folder)
    object Search : BottomNavItem(Screen.Search.route, "البحث", Icons.Default.Search)
}
ENDOFFILE

# --- ui/viewmodel/MessageViewModel.kt ---
cat > "$PROJECT_DIR/app/src/main/java/com/example/whatsappsaver/ui/viewmodel/MessageViewModel.kt" << 'ENDOFFILE'
package com.example.whatsappsaver.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.whatsappsaver.data.local.entity.Message
import com.example.whatsappsaver.data.repository.MessageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MessageViewModel @Inject constructor(
    private val repository: MessageRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    val allMessages: StateFlow<List<Message>> = repository.getAllMessages()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categories: StateFlow<List<String>> = repository.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredMessages: StateFlow<List<Message>> = combine(
        allMessages,
        _searchQuery,
        _selectedCategory
    ) { messages, query, category ->
        var result = messages

        // Filter by category if selected
        if (!category.isNullOrBlank() && category != "الكل") {
            result = result.filter { it.category == category }
        }

        // Filter by search query
        if (query.isNotBlank()) {
            result = result.filter {
                it.messageText.contains(query, ignoreCase = true) ||
                it.notes.contains(query, ignoreCase = true)
            }
        }

        result
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setCategory(category: String?) {
        _selectedCategory.value = category
    }

    fun insertMessage(message: Message) {
        viewModelScope.launch {
            repository.insertMessage(message)
        }
    }

    fun updateMessage(message: Message) {
        viewModelScope.launch {
            repository.updateMessage(message)
        }
    }

    fun deleteMessage(message: Message) {
        viewModelScope.launch {
            repository.deleteMessage(message)
        }
    }

    fun togglePin(message: Message) {
        viewModelScope.launch {
            repository.togglePinStatus(message)
        }
    }

    fun getMessageById(id: Int): Flow<Message?> = flow {
        emit(repository.getMessageById(id))
    }
}
ENDOFFILE

# --- ui/theme/Color.kt ---
cat > "$PROJECT_DIR/app/src/main/java/com/example/whatsappsaver/ui/theme/Color.kt" << 'ENDOFFILE'
package com.example.whatsappsaver.ui.theme

import androidx.compose.ui.graphics.Color

val PrimaryLight = Color(0xFF075E54)      // WhatsApp Green
val PrimaryDark = Color(0xFF128C7E)
val SecondaryLight = Color(0xFF25D366)    // WhatsApp Light Green
val SecondaryDark = Color(0xFF34B7F1)     // WhatsApp Blue

val BackgroundLight = Color(0xFFF0F2F5)
val BackgroundDark = Color(0xFF121212)
val SurfaceLight = Color(0xFFFFFFFF)
val SurfaceDark = Color(0xFF1E1E1E)

val ErrorLight = Color(0xFFB00020)
val ErrorDark = Color(0xFFCF6679)

val OnPrimaryLight = Color(0xFFFFFFFF)
val OnPrimaryDark = Color(0xFFFFFFFF)
val OnBackgroundLight = Color(0xFF1C1E21)
val OnBackgroundDark = Color(0xFFE4E6EB)
ENDOFFILE

# --- ui/theme/Type.kt ---
cat > "$PROJECT_DIR/app/src/main/java/com/example/whatsappsaver/ui/theme/Type.kt" << 'ENDOFFILE'
package com.example.whatsappsaver.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Typography = Typography(
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp
    )
)
ENDOFFILE

# --- ui/theme/Theme.kt ---
cat > "$PROJECT_DIR/app/src/main/java/com/example/whatsappsaver/ui/theme/Theme.kt" << 'ENDOFFILE'
package com.example.whatsappsaver.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryDark,
    secondary = SecondaryDark,
    tertiary = SecondaryLight,
    background = BackgroundDark,
    surface = SurfaceDark,
    error = ErrorDark,
    onPrimary = OnPrimaryDark,
    onSecondary = androidx.compose.ui.graphics.Color.White,
    onTertiary = androidx.compose.ui.graphics.Color.White,
    onBackground = OnBackgroundDark,
    onSurface = OnBackgroundDark,
    onError = androidx.compose.ui.graphics.Color.Black
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryLight,
    secondary = SecondaryLight,
    tertiary = SecondaryDark,
    background = BackgroundLight,
    surface = SurfaceLight,
    error = ErrorLight,
    onPrimary = OnPrimaryLight,
    onSecondary = androidx.compose.ui.graphics.Color.White,
    onTertiary = androidx.compose.ui.graphics.Color.White,
    onBackground = OnBackgroundLight,
    onSurface = OnBackgroundLight,
    onError = androidx.compose.ui.graphics.Color.White
)

@Composable
fun WhatsAppSaverTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
ENDOFFILE

# --- ui/screens/HomeScreen.kt ---
cat > "$PROJECT_DIR/app/src/main/java/com/example/whatsappsaver/ui/screens/HomeScreen.kt" << 'ENDOFFILE'
package com.example.whatsappsaver.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.whatsappsaver.data.local.entity.Message
import com.example.whatsappsaver.ui.navigation.Screen
import com.example.whatsappsaver.ui.viewmodel.MessageViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: MessageViewModel = hiltViewModel()
) {
    val messages by viewModel.filteredMessages.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("رسائل المحفوظة") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.AddMessage.createRoute()) },
                containerColor = MaterialTheme.colorScheme.secondary
            ) {
                Icon(Icons.Default.Add, contentDescription = "إضافة رسالة")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Category Filter Chips
            if (categories.isNotEmpty()) {
                ScrollableCategoryChips(
                    categories = categories,
                    selectedCategory = selectedCategory,
                    onCategorySelected = { viewModel.setCategory(it) }
                )
            }

            if (messages.isEmpty()) {
                EmptyState()
            } else {
                MessagesList(
                    messages = messages,
                    onMessageClick = { messageId ->
                        navController.navigate(Screen.MessageDetail.createRoute(messageId))
                    },
                    onPinClick = { viewModel.togglePin(it) },
                    onDeleteClick = { viewModel.deleteMessage(it) }
                )
            }
        }
    }
}

@Composable
fun ScrollableCategoryChips(
    categories: List<String>,
    selectedCategory: String?,
    onCategorySelected: (String?) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = selectedCategory == null || selectedCategory == "الكل",
            onClick = { onCategorySelected("الكل") },
            label = { Text("الكل") }
        )
        categories.forEach { category ->
            FilterChip(
                selected = selectedCategory == category,
                onClick = { onCategorySelected(category) },
                label = { Text(category) }
            )
        }
    }
}

@Composable
fun MessagesList(
    messages: List<Message>,
    onMessageClick: (Int) -> Unit,
    onPinClick: (Message) -> Unit,
    onDeleteClick: (Message) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(messages, key = { it.id }) { message ->
            MessageCard(
                message = message,
                onClick = { onMessageClick(message.id) },
                onPinClick = { onPinClick(message) },
                onDeleteClick = { onDeleteClick(message) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageCard(
    message: Message,
    onClick: () -> Unit,
    onPinClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    val dateStr = dateFormat.format(Date(message.timestamp))

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = message.category,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                if (message.isPinned) {
                    Icon(
                        imageVector = Icons.Default.PushPin,
                        contentDescription = "مثبت",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = message.messageText,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            if (message.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "ملاحظة: ${message.notes}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = dateStr,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row {
                    IconButton(onClick = onPinClick) {
                        Icon(
                            imageVector = Icons.Default.PushPin,
                            contentDescription = if (message.isPinned) "إلغاء التثبيت" else "تثبيت",
                            tint = if (message.isPinned) MaterialTheme.colorScheme.secondary
                                   else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDeleteClick) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "حذف",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Message,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Text(
                text = "لا توجد رسائل محفوظة",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "اضغط على الزر + لإضافة رسالة جديدة",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}
ENDOFFILE

# --- ui/screens/AddMessageScreen.kt ---
cat > "$PROJECT_DIR/app/src/main/java/com/example/whatsappsaver/ui/screens/AddMessageScreen.kt" << 'ENDOFFILE'
package com.example.whatsappsaver.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.whatsappsaver.data.local.entity.Message
import com.example.whatsappsaver.ui.viewmodel.MessageViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMessageScreen(
    navController: NavController,
    sharedText: String = "",
    viewModel: MessageViewModel = hiltViewModel()
) {
    var messageText by remember { mutableStateOf(sharedText) }
    var notes by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("عام") }
    var showCategoryDialog by remember { mutableStateOf(false) }
    val categories by viewModel.categories.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("إضافة رسالة جديدة") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "رجوع")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Message Text Field
            OutlinedTextField(
                value = messageText,
                onValueChange = { messageText = it },
                label = { Text("نص الرسالة *") },
                placeholder = { Text("الصق نص الرسالة هنا...") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 5,
                maxLines = 10,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )

            // Category Selection
            OutlinedCard(
                onClick = { showCategoryDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "التصنيف: $category",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Icon(Icons.Default.ArrowDropDown, contentDescription = "اختيار التصنيف")
                }
            }

            // Notes Field
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("ملاحظات (اختياري)") },
                placeholder = { Text("أضف ملاحظاتك هنا...") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5
            )

            Spacer(modifier = Modifier.weight(1f))

            // Save Button
            Button(
                onClick = {
                    if (messageText.isNotBlank()) {
                        val message = Message(
                            messageText = messageText.trim(),
                            notes = notes.trim(),
                            category = category
                        )
                        viewModel.insertMessage(message)
                        navController.popBackStack()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = messageText.isNotBlank()
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("حفظ الرسالة")
            }
        }
    }

    // Category Selection Dialog
    if (showCategoryDialog) {
        AlertDialog(
            onDismissRequest = { showCategoryDialog = false },
            title = { Text("اختر التصنيف") },
            text = {
                Column {
                    val allCategories = (categories + listOf("عام", "عمل", "عائلة", "أصدقاء", "مهم")).distinct()
                    allCategories.forEach { cat ->
                        TextButton(
                            onClick = {
                                category = cat
                                showCategoryDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(cat)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCategoryDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }
}
ENDOFFILE

# --- ui/screens/MessageDetailScreen.kt ---
cat > "$PROJECT_DIR/app/src/main/java/com/example/whatsappsaver/ui/screens/MessageDetailScreen.kt" << 'ENDOFFILE'
package com.example.whatsappsaver.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.whatsappsaver.ui.viewmodel.MessageViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageDetailScreen(
    navController: NavController,
    messageId: Int,
    viewModel: MessageViewModel = hiltViewModel()
) {
    val message by viewModel.getMessageById(messageId).collectAsState(initial = null)
    val clipboardManager = LocalClipboardManager.current
    var showDeleteDialog by remember { mutableStateOf(false) }

    message?.let { msg ->
        val dateFormat = SimpleDateFormat("EEEE, dd MMMM yyyy - HH:mm", Locale("ar"))
        val dateStr = dateFormat.format(Date(msg.timestamp))

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("تفاصيل الرسالة") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "رجوع")
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            viewModel.togglePin(msg)
                        }) {
                            Icon(
                                imageVector = Icons.Default.PushPin,
                                contentDescription = "تثبيت",
                                tint = if (msg.isPinned) MaterialTheme.colorScheme.secondary
                                       else LocalContentColor.current
                            )
                        }
                        IconButton(onClick = {
                            clipboardManager.setText(AnnotatedString(msg.messageText))
                        }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "نسخ")
                        }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "حذف", tint = MaterialTheme.colorScheme.error)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                        actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Category Chip
                AssistChip(
                    onClick = { },
                    label = { Text(msg.category) },
                    leadingIcon = { Icon(Icons.Default.Label, null, Modifier.size(18.dp)) }
                )

                // Message Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(
                        text = msg.messageText,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                // Notes
                if (msg.notes.isNotBlank()) {
                    Text(
                        text = "الملاحظات:",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Text(
                            text = msg.notes,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Date
                Text(
                    text = "تاريخ الحفظ: $dateStr",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("تأكيد الحذف") },
                text = { Text("هل أنت متأكد من حذف هذه الرسالة؟ لا يمكن التراجع عن هذا الإجراء.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.deleteMessage(msg)
                            showDeleteDialog = false
                            navController.popBackStack()
                        },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("حذف")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("إلغاء")
                    }
                }
            )
        }
    } ?: run {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}
ENDOFFILE

# --- ui/screens/CategoriesScreen.kt ---
cat > "$PROJECT_DIR/app/src/main/java/com/example/whatsappsaver/ui/screens/CategoriesScreen.kt" << 'ENDOFFILE'
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
fun CategoriesScreen(
    navController: NavController,
    viewModel: MessageViewModel = hiltViewModel()
) {
    val categories by viewModel.categories.collectAsState()
    val allMessages by viewModel.allMessages.collectAsState()

    // Group messages by month
    val groupedByMonth = remember(allMessages) {
        allMessages.groupBy {
            val cal = java.util.Calendar.getInstance()
            cal.timeInMillis = it.timestamp
            "${cal.get(java.util.Calendar.YEAR)}-${cal.get(java.util.Calendar.MONTH)}"
        }.mapKeys { (key, _) ->
            val parts = key.split("-")
            val monthNames = listOf("يناير", "فبراير", "مارس", "أبريل", "مايو", "يونيو",
                "يوليو", "أغسطس", "سبتمبر", "أكتوبر", "نوفمبر", "ديسمبر")
            "${monthNames[parts[1].toInt()]} ${parts[0]}"
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("التصنيفات والأرشيف") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Custom Categories Section
            item {
                Text(
                    text = "التصنيفات المخصصة",
                    style = MaterialTheme.typography.titleLarge
                )
            }
            items(categories) { category ->
                CategoryItem(
                    name = category,
                    count = allMessages.count { it.category == category }
                ) {
                    viewModel.setCategory(category)
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            }

            item { HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp)) }

            // Monthly Archive Section
            item {
                Text(
                    text = "الأرشيف الشهري",
                    style = MaterialTheme.typography.titleLarge
                )
            }
            items(groupedByMonth.toList()) { (month, msgs) ->
                CategoryItem(
                    name = month,
                    count = msgs.size
                ) {
                    // Filter by month logic can be added here
                }
            }
        }
    }
}

@Composable
fun CategoryItem(name: String, count: Int, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Folder,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            Badge { Text("$count") }
        }
    }
}
ENDOFFILE

# --- ui/screens/SearchScreen.kt ---
cat > "$PROJECT_DIR/app/src/main/java/com/example/whatsappsaver/ui/screens/SearchScreen.kt" << 'ENDOFFILE'
package com.example.whatsappsaver.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.whatsappsaver.ui.navigation.Screen
import com.example.whatsappsaver.ui.viewmodel.MessageViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    navController: NavController,
    viewModel: MessageViewModel = hiltViewModel()
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val messages by viewModel.filteredMessages.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.setSearchQuery(it) },
                        placeholder = { Text("ابحث في الرسائل...") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.Search, null) }
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (searchQuery.isBlank()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("اكتب كلمة للبحث", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else if (messages.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("لا توجد نتائج", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(messages, key = { it.id }) { message ->
                        MessageCard(
                            message = message,
                            onClick = { navController.navigate(Screen.MessageDetail.createRoute(message.id)) },
                            onPinClick = { viewModel.togglePin(message) },
                            onDeleteClick = { viewModel.deleteMessage(message) }
                        )
                    }
                }
            }
        }
    }
}
ENDOFFILE

echo "✅ All Kotlin source files created."

# ============================================================
# GITHUB ACTIONS WORKFLOW - Build APK
# ============================================================

echo "📝 Creating GitHub Actions workflow..."

cat > "$PROJECT_DIR/.github/workflows/build.yml" << 'ENDOFFILE'
name: Build Android APK

on:
  push:
    branches: [ main, master ]
  pull_request:
    branches: [ main, master ]
  workflow_dispatch:

jobs:
  build:
    runs-on: ubuntu-latest

    steps:
      - name: Checkout code
        uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'

      - name: Grant execute permission for gradlew
        run: chmod +x gradlew

      - name: Build Debug APK
        run: ./gradlew assembleDebug

      - name: Build Release APK
        run: ./gradlew assembleRelease

      - name: Upload Debug APK
        uses: actions/upload-artifact@v4
        with:
          name: debug-apk
          path: app/build/outputs/apk/debug/app-debug.apk

      - name: Upload Release APK
        uses: actions/upload-artifact@v4
        with:
          name: release-apk
          path: app/build/outputs/apk/release/app-release-unsigned.apk
ENDOFFILE

echo "✅ GitHub Actions workflow created."

# ============================================================
# README.md
# ============================================================

cat > "$PROJECT_DIR/README.md" << 'ENDOFFILE'
# WhatsApp Saver 📱

تطبيق أندرويد لحفظ رسائل واتساب المهمة مع إمكانية التصنيف والبحث والأرشيف.

## المميزات

- حفظ رسائل واتساب المستلمة عبر Share Intent
- تصنيف الرسائل في فئات مخصصة
- البحث في الرسائل والملاحظات
- تثبيت الرسائل المهمة
- أرشيف شهري للرسائل
- واجهة مستخدم عربية بالكامل
- دعم الوضع المظلم

## التقنيات المستخدمة

- **Kotlin** - لغة البرمجة
- **Jetpack Compose** - واجهة المستخدم
- **Room** - قاعدة البيانات المحلية
- **Hilt** - حقن التبعيات
- **Navigation Compose** - التنقل بين الشاشات
- **Material 3** - التصميم

## البناء

```bash
# بناء APK للتطوير
./gradlew assembleDebug

# بناء APK للإصدار
./gradlew assembleRelease 