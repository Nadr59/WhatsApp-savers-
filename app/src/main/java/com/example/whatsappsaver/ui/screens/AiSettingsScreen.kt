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

    var provider: String by remember { mutableStateOf(settings.provider) }
    var openaiKey: String by remember { mutableStateOf(settings.openaiKey) }
    var geminiKey: String by remember { mutableStateOf(settings.geminiKey) }
    var mistralKey: String by remember { mutableStateOf(settings.mistralKey) }
    var openrouterKey: String by remember { mutableStateOf(settings.openrouterKey) }
    var openrouterModel: String by remember { mutableStateOf(settings.openrouterModel) }
    var groqKey: String by remember { mutableStateOf(settings.groqKey) }
    var groqModel: String by remember { mutableStateOf(settings.groqModel) }
    var customUrl: String by remember { mutableStateOf(settings.customUrl) }
    var customKey: String by remember { mutableStateOf(settings.customKey) }
    var customModel: String by remember { mutableStateOf(settings.customModel) }
    var showKeys: Boolean by remember { mutableStateOf(false) }
    var saved: Boolean by remember { mutableStateOf(false) }

    val providers = listOf(
        "groq" to "Groq (مجاني - الأسرع)",
        "openrouter" to "OpenRouter",
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
                        Text(
                            name,
                            fontWeight = if (provider == key) FontWeight.Bold else FontWeight.Normal
                        )
                        if (provider == key && key == settings.provider && settings.isConfigured()) {
                            Spacer(Modifier.weight(1f))
                            Icon(Icons.Default.CheckCircle, null, tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }

            Divider(
                modifier = Modifier.fillMaxWidth(),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
            )

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

            when (provider) {

                // ═══ Groq ═══
                "groq" -> {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text(
                                "Groq — مجاني تماماً وسريع جداً",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.height(4.dp))
                            Text("1. سجل على console.groq.com", style = MaterialTheme.typography.bodySmall)
                            Text("2. أنشئ مفتاح من: console.groq.com/keys", style = MaterialTheme.typography.bodySmall)
                            Text("3. مجاني بدون أي رصيد!", style = MaterialTheme.typography.bodySmall)
                        }
                    }

                    ApiKeyField(
                        label = "Groq API Key",
                        value = groqKey,
                        onValueChange = { groqKey = it; saved = false },
                        showKey = showKeys,
                        placeholder = "gsk_..."
                    )

                    Spacer(Modifier.height(4.dp))
                    Text("النموذج:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)

                    val groqModels = listOf(
                        "llama-3.1-8b-instant" to "Llama 3.1 8B (سريع)",
                        "llama-3.3-70b-versatile" to "Llama 3.3 70B (أفضل جودة)",
                        "gemma2-9b-it" to "Gemma 2 9B",
                        "mixtral-8x7b-32768" to "Mixtral 8x7B"
                    )

                    ModelDropdown(
                        models = groqModels,
                        selectedModel = groqModel,
                        onModelSelected = { groqModel = it; saved = false }
                    )
                }

                // ═══ OpenRouter ═══
                "openrouter" -> {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text(
                                "OpenRouter",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.height(4.dp))
                            Text("1. سجل على openrouter.ai", style = MaterialTheme.typography.bodySmall)
                            Text("2. أنشئ مفتاح من: openrouter.ai/keys", style = MaterialTheme.typography.bodySmall)
                            Text("3. أضف رصيد من: openrouter.ai/credits", style = MaterialTheme.typography.bodySmall)
                        }
                    }

                    ApiKeyField(
                        label = "OpenRouter API Key",
                        value = openrouterKey,
                        onValueChange = { openrouterKey = it; saved = false },
                        showKey = showKeys,
                        placeholder = "sk-or-v1-..."
                    )

                    Spacer(Modifier.height(4.dp))
                    Text("النموذج:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)

                    val orModels = listOf(
                        "google/gemini-2.0-flash-exp" to "Gemini 2.0 Flash",
                        "meta-llama/llama-3.1-8b-instruct" to "Llama 3.1 8B",
                        "mistralai/mistral-7b-instruct" to "Mistral 7B",
                        "deepseek/deepseek-chat-v3-0324" to "DeepSeek V3",
                        "openai/gpt-3.5-turbo" to "GPT-3.5 Turbo"
                    )

                    ModelDropdown(
                        models = orModels,
                        selectedModel = openrouterModel,
                        onModelSelected = { openrouterModel = it; saved = false }
                    )

                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = openrouterModel,
                        onValueChange = { openrouterModel = it; saved = false },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("أو أدخل اسم النموذج يدوياً") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // ═══ OpenAI ═══
                "openai" -> {
                    ApiKeyField(
                        label = "OpenAI API Key",
                        value = openaiKey,
                        onValueChange = { openaiKey = it; saved = false },
                        showKey = showKeys,
                        placeholder = "sk-..."
                    )
                }

                // ═══ Gemini ═══
                "gemini" -> {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            "ملاحظة: قد تكون حصة المجانية مستنفدة",
                            Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    ApiKeyField(
                        label = "Gemini API Key",
                        value = geminiKey,
                        onValueChange = { geminiKey = it; saved = false },
                        showKey = showKeys,
                        placeholder = "AIza..."
                    )
                }

                // ═══ Mistral ═══
                "mistral" -> {
                    ApiKeyField(
                        label = "Mistral API Key",
                        value = mistralKey,
                        onValueChange = { mistralKey = it; saved = false },
                        showKey = showKeys,
                        placeholder = "مفتاح Mistral"
                    )
                }

                // ═══ Custom ═══
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

            Button(
                onClick = {
                    viewModel.saveAiProvider(provider)
                    when (provider) {
                        "groq" -> viewModel.saveGroqConfig(groqKey, groqModel)
                        "openrouter" -> viewModel.saveOpenRouterConfig(openrouterKey, openrouterModel)
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

            Spacer(Modifier.height(32.dp))
        }
    }
}

// ═══ كومبوننت منفصل لقائمة النماذج ═══
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModelDropdown(
    models: List<Pair<String, String>>,
    selectedModel: String,
    onModelSelected: (String) -> Unit
) {
    var expanded: Boolean by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = models.find { it.first == selectedModel }?.second ?: selectedModel,
            onValueChange = {},
            readOnly = true,
            modifier = Modifier.fillMaxWidth().menuAnchor(),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            shape = RoundedCornerShape(12.dp),
            label = { Text("اختر النموذج") }
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            models.forEach { (modelId, modelName) ->
                DropdownMenuItem(
                    text = { Text(modelName) },
                    onClick = {
                        onModelSelected(modelId)
                        expanded = false
                    }
                )
            }
        }
    }
}

// ═══ كومبوننت حقل مفتاح API ═══
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
