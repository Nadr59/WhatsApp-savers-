package com.example.whatsappsaver.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.whatsappsaver.ui.viewmodel.MessageViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiSettingsScreen(
    navController: NavController,
    viewModel: MessageViewModel
) {
    val settings = viewModel.getAiSettings()
    var provider by remember { mutableStateOf(settings.provider) }
    var openaiKey by remember { mutableStateOf(settings.openaiKey) }
    var geminiKey by remember { mutableStateOf(settings.geminiKey) }
    var mistralKey by remember { mutableStateOf(settings.mistralKey) }
    var customUrl by remember { mutableStateOf(settings.customUrl) }
    var customKey by remember { mutableStateOf(settings.customKey) }
    var customModel by remember { mutableStateOf(settings.customModel) }
    var showKeys by remember { mutableStateOf(false) }
    var saved by remember { mutableStateOf(false) }

    val providers = listOf(
        "openai" to "OpenAI (ChatGPT)",
        "gemini" to "Google Gemini",
        "mistral" to "Mistral AI",
        "custom" to "مخصص (OpenAI Compatible)"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("إعدادات الذكاء الاصطناعي") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "رجوع")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ═══ اختيار المزود ═══
            Text("مزود الخدمة:", fontWeight = FontWeight.Bold)
            providers.forEach { (key, name) ->
                Card(
                    onClick = { provider = key; saved = false },
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (provider == key)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = provider == key,
                            onClick = { provider = key; saved = false }
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(name, fontWeight = if (provider == key) FontWeight.Bold else FontWeight.Normal)
                        if (provider == key && key == settings.provider && settings.isConfigured()) {
                            Spacer(Modifier.weight(1f))
                            Icon(Icons.Default.CheckCircle, null, tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }

            HorizontalDivider()

            // ═══ زر إظهار/إخفاء المفاتيح ═══
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("مفاتيح API:", fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                IconButton(onClick = { showKeys = !showKeys }) {
                    Icon(
                        if (showKeys) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        if (showKeys) "إخفاء" else "إظهار"
                    )
                }
            }

            // ═══ حقول المفاتيح ═══
            when (provider) {
                "openai" -> {
                    ApiKeyField(
                        label = "OpenAI API Key",
                        value = openaiKey,
                        onValueChange = { openaiKey = it; saved = false },
                        showKey = showKeys,
                        placeholder = "sk-..."
                    )
                }
                "gemini" -> {
                    ApiKeyField(
                        label = "Gemini API Key",
                        value = geminiKey,
                        onValueChange = { geminiKey = it; saved = false },
                        showKey = showKeys,
                        placeholder = "AIza..."
                    )
                }
                "mistral" -> {
                    ApiKeyField(
                        label = "Mistral API Key",
                        value = mistralKey,
                        onValueChange = { mistralKey = it; saved = false },
                        showKey = showKeys,
                        placeholder = "مفتاح Mistral"
                    )
                }
                "custom" -> {
                    OutlinedTextField(
                        value = customUrl,
                        onValueChange = { customUrl = it; saved = false },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("رابط الخادم") },
                        placeholder = { Text("https://api.example.com") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    ApiKeyField(
                        label = "API Key",
                        value = customKey,
                        onValueChange = { customKey = it; saved = false },
                        showKey = showKeys,
                        placeholder = "مفتاح API"
                    )
                    OutlinedTextField(
                        value = customModel,
                        onValueChange = { customModel = it; saved = false },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("اسم النموذج") },
                        placeholder = { Text("gpt-3.5-turbo") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // ═══ زر الحفظ ═══
            Button(
                onClick = {
                    viewModel.saveAiProvider(provider)
                    when (provider) {
                        "openai" -> viewModel.saveOpenAiKey(openaiKey)
                        "gemini" -> viewModel.saveGeminiKey(geminiKey)
                        "mistral" -> viewModel.saveMistralKey(mistralKey)
                        "custom" -> viewModel.saveCustomConfig(customUrl, customKey, customModel)
                    }
                    saved = true
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Save, null)
                Spacer(Modifier.width(8.dp))
                Text("حفظ الإعدادات", fontWeight = FontWeight.Bold)
            }

            if (saved) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(8.dp))
                        Text("تم الحفظ بنجاح!", color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            // ═══ معلومات ═══
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("كيف تحصل على مفتاح API:", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text("OpenAI: platform.openai.com/api-keys", style = MaterialTheme.typography.bodySmall)
                    Text("Gemini: makersuite.google.com/app/apikey", style = MaterialTheme.typography.bodySmall)
                    Text("Mistral: console.mistral.ai", style = MaterialTheme.typography.bodySmall)
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
fun ApiKeyField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    showKey: Boolean,
    placeholder: String
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        singleLine = true,
        visualTransformation = if (showKey) VisualTransformation.None
        else PasswordVisualTransformation(),
        shape = RoundedCornerShape(12.dp)
    )
}
