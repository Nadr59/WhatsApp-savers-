package com.example.whatsappsaver.data.local

import android.content.Context
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AiSettings @Inject constructor(context: Context) {

    private val prefs = context.getSharedPreferences("ai_settings", Context.MODE_PRIVATE)

    var provider: String
        get() = prefs.getString("provider", "groq") ?: "groq"
        set(value) = prefs.edit().putString("provider", value).apply()

    var openaiKey: String
        get() = prefs.getString("openai_key", "") ?: ""
        set(value) = prefs.edit().putString("openai_key", value).apply()

    var geminiKey: String
        get() = prefs.getString("gemini_key", "") ?: ""
        set(value) = prefs.edit().putString("gemini_key", value).apply()

    var mistralKey: String
        get() = prefs.getString("mistral_key", "") ?: ""
        set(value) = prefs.edit().putString("mistral_key", value).apply()

    var openrouterKey: String
        get() = prefs.getString("openrouter_key", "") ?: ""
        set(value) = prefs.edit().putString("openrouter_key", value).apply()

    var openrouterModel: String
        get() = prefs.getString("openrouter_model", "google/gemini-2.0-flash-exp") ?: "google/gemini-2.0-flash-exp"
        set(value) = prefs.edit().putString("openrouter_model", value).apply()

    var groqKey: String
        get() = prefs.getString("groq_key", "") ?: ""
        set(value) = prefs.edit().putString("groq_key", value).apply()

    var groqModel: String
        get() = prefs.getString("groq_model", "llama-3.1-8b-instant") ?: "llama-3.1-8b-instant"
        set(value) = prefs.edit().putString("groq_model", value).apply()

    var customUrl: String
        get() = prefs.getString("custom_url", "") ?: ""
        set(value) = prefs.edit().putString("custom_url", value).apply()

    var customKey: String
        get() = prefs.getString("custom_key", "") ?: ""
        set(value) = prefs.edit().putString("custom_key", value).apply()

    var customModel: String
        get() = prefs.getString("custom_model", "gpt-3.5-turbo") ?: "gpt-3.5-turbo"
        set(value) = prefs.edit().putString("custom_model", value).apply()

    var defaultLanguage: String
        get() = prefs.getString("default_lang", "العربية") ?: "العربية"
        set(value) = prefs.edit().putString("default_lang", value).apply()

    fun getActiveKey(): String {
        return when (provider) {
            "openai" -> openaiKey
            "gemini" -> geminiKey
            "mistral" -> mistralKey
            "openrouter" -> openrouterKey
            "groq" -> groqKey
            "custom" -> customKey
            else -> ""
        }
    }

    fun isConfigured(): Boolean {
        return getActiveKey().isNotBlank()
    }
}
