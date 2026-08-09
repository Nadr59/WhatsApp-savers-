package com.example.whatsappsaver.data.ai

import com.example.whatsappsaver.data.local.AiSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AiRepository @Inject constructor(private val settings: AiSettings) {

    suspend fun process(
        text: String,
        task: String,
        language: String = settings.defaultLanguage
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            if (!settings.isConfigured()) {
                return@withContext Result.failure(Exception("يرجى إعداد مفتاح AI أولاً من الإعدادات"))
            }

            val prompt = buildPrompt(text, task, language)

            val result = when (settings.provider) {
                "openai" -> callOpenAI(prompt)
                "gemini" -> callGemini(prompt)
                "mistral" -> callMistral(prompt)
                "custom" -> callCustom(prompt)
                else -> return@withContext Result.failure(Exception("مزود غير معروف"))
            }

            Result.success(result)
        } catch (e: Exception) {
            Result.failure(Exception("خطأ في الاتصال: ${e.message}"))
        }
    }

    private fun buildPrompt(text: String, task: String, language: String): String {
        return when (task) {
            "شرح" -> "اشرح هذا النص بوضوح وبساطة باللغة $language:\n\n$text"
            "استخراج معلومات" -> "استخرج جميع المعلومات المهمة من هذا النص (أسماء، أرقام، تواريخ، أماكن) وأعرضها كقائمة مرتبة باللغة $language:\n\n$text"
            "تلخيص" -> "لخص هذا النص بشكل مختصر وشامل باللغة $language:\n\n$text"
            "ترجمة إلى الإنجليزية" -> "ترجم هذا النص إلى الإنجليزية:\n\n$text"
            "ترجمة إلى العربية" -> "ترجم هذا النص إلى العربية:\n\n$text"
            "اقتراح ردود" -> "اقترح 3 ردود مناسبة لهذا النص باللغة $language:\n\n$text"
            "تحليل المشاعر" -> "حلل المشاعر والنبرة في هذا النص (إيجابي/سلبي/محايد) مع التفسير باللغة $language:\n\n$text"
            "تحليل الأولوية" -> "حدد مدى أهمية وأولوية هذا النص (عاجل/مهم/عادي) مع التفسير باللغة $language:\n\n$text"
            "استخراج المهام" -> "استخرج أي مهام أو إجراءات مطلوبة من هذا النص وأعرضها كقائمة باللغة $language:\n\n$text"
            else -> "حلل هذا النص باللغة $language:\n\n$text"
        }
    }

    private fun callOpenAI(prompt: String): String {
        val url = URL("https://api.openai.com/v1/chat/completions")
        val conn = url.openConnection() as HttpURLConnection
        conn.apply {
            requestMethod = "POST"
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Authorization", "Bearer ${settings.openaiKey}")
            connectTimeout = 30000
            readTimeout = 30000
            doOutput = true
        }

        val body = JSONObject().apply {
            put("model", settings.customModel)
            put("messages", JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", prompt)
                })
            })
            put("max_tokens", 1000)
            put("temperature", 0.7)
        }

        OutputStreamWriter(conn.outputStream).use { it.write(body.toString()) }

        val response = conn.inputStream.bufferedReader().readText()
        val json = JSONObject(response)
        return json.getJSONArray("choices")
            .getJSONObject(0)
            .getJSONObject("message")
            .getString("content")
            .trim()
    }

    private fun callGemini(prompt: String): String {
        val url = URL("https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent?key=${settings.geminiKey}")
        val conn = url.openConnection() as HttpURLConnection
        conn.apply {
            requestMethod = "POST"
            setRequestProperty("Content-Type", "application/json")
            connectTimeout = 30000
            readTimeout = 30000
            doOutput = true
        }

        val body = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", prompt)
                        })
                    })
                })
            })
        }

        OutputStreamWriter(conn.outputStream).use { it.write(body.toString()) }

        val response = conn.inputStream.bufferedReader().readText()
        val json = JSONObject(response)
        return json.getJSONArray("candidates")
            .getJSONObject(0)
            .getJSONObject("content")
            .getJSONArray("parts")
            .getJSONObject(0)
            .getString("text")
            .trim()
    }

    private fun callMistral(prompt: String): String {
        val url = URL("https://api.mistral.ai/v1/chat/completions")
        val conn = url.openConnection() as HttpURLConnection
        conn.apply {
            requestMethod = "POST"
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Authorization", "Bearer ${settings.mistralKey}")
            connectTimeout = 30000
            readTimeout = 30000
            doOutput = true
        }

        val body = JSONObject().apply {
            put("model", "mistral-tiny")
            put("messages", JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", prompt)
                })
            })
            put("max_tokens", 1000)
        }

        OutputStreamWriter(conn.outputStream).use { it.write(body.toString()) }

        val response = conn.inputStream.bufferedReader().readText()
        val json = JSONObject(response)
        return json.getJSONArray("choices")
            .getJSONObject(0)
            .getJSONObject("message")
            .getString("content")
            .trim()
    }

    private fun callCustom(prompt: String): String {
        val baseUrl = settings.customUrl.trimEnd('/')
        val url = URL("$baseUrl/v1/chat/completions")
        val conn = url.openConnection() as HttpURLConnection
        conn.apply {
            requestMethod = "POST"
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Authorization", "Bearer ${settings.customKey}")
            connectTimeout = 30000
            readTimeout = 30000
            doOutput = true
        }

        val body = JSONObject().apply {
            put("model", settings.customModel)
            put("messages", JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", prompt)
                })
            })
            put("max_tokens", 1000)
        }

        OutputStreamWriter(conn.outputStream).use { it.write(body.toString()) }

        val response = conn.inputStream.bufferedReader().readText()
        val json = JSONObject(response)
        return json.getJSONArray("choices")
            .getJSONObject(0)
            .getJSONObject("message")
            .getString("content")
            .trim()
    }
}
