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
                return@withContext Result.failure(
                    Exception("يرجى إعداد مفتاح AI أولاً من الإعدادات")
                )
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

    private fun getErrorMessage(conn: HttpURLConnection): String {
        return try {
            val errorStream = conn.errorStream
            if (errorStream != null) {
                val errorText = errorStream.bufferedReader().readText()
                val json = JSONObject(errorText)
                json.optJSONObject("error")?.optString("message")
                    ?: errorText.take(300)
            } else {
                "HTTP ${conn.responseCode}: ${conn.responseMessage}"
            }
        } catch (_: Exception) {
            "HTTP ${conn.responseCode}: ${conn.responseMessage}"
        }
    }

    // ═══════════════════════════════════════════════════════════
    // ═══ Gemini — نماذج حقيقية فقط + تشخيص واضح ═══
    // ═══════════════════════════════════════════════════════════
    private fun callGemini(prompt: String): String {
        val apiKey = settings.geminiKey.trim()

        if (apiKey.isBlank()) {
            throw Exception("مفتاح Gemini فارغ. أضف المفتاح من الإعدادات")
        }

        // ═══ النماذج المؤكدة فعلياً في Google API ═══
        val models = listOf(
            "gemini-2.0-flash",
            "gemini-1.5-flash",
            "gemini-1.5-pro"
        )

        val errors = mutableListOf<String>()

        for (model in models) {
            try {
                val result = callGeminiModel(apiKey, model, prompt)
                if (result.isNotBlank()) {
                    return result
                }
                errors.add("$model: استجابة فارغة")
            } catch (e: Exception) {
                val msg = e.message ?: "خطأ غير معروف"
                errors.add("$model: $msg")
            }
        }

        // ═══ جميع النماذج فشلت — رسالة واضحة ═══
        val errorSummary = errors.joinToString("\n") { "  - $it" }
        throw Exception(
            "فشل الاتصال بـ Gemini:\n$errorSummary\n\n" +
            "الحل الأسرع:\n" +
            "1. أنشئ مفتاح جديد من:\n" +
            "   aistudio.google.com/apikey\n" +
            "2. أو جرب مزود OpenAI أو Mistral\n" +
            "   من إعدادات التطبيق"
        )
    }

    private fun callGeminiModel(apiKey: String, model: String, prompt: String): String {
        val urlStr = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"
        val url = URL(urlStr)
        val conn = url.openConnection() as HttpURLConnection
        conn.apply {
            requestMethod = "POST"
            setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            connectTimeout = 60000
            readTimeout = 60000
            doOutput = true
            doInput = true
        }

        val body = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "user")
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", prompt)
                        })
                    })
                })
            })
            put("generationConfig", JSONObject().apply {
                put("temperature", 0.7)
                put("maxOutputTokens", 2048)
            })
        }

        try {
            OutputStreamWriter(conn.outputStream, Charsets.UTF_8).use {
                it.write(body.toString())
                it.flush()
            }
        } catch (e: Exception) {
            throw Exception("فشل إرسال الطلب: ${e.message}")
        }

        val responseCode = conn.responseCode

        if (responseCode != 200) {
            val errorMsg = getErrorMessage(conn)
            conn.disconnect()
            throw Exception(errorMsg)
        }

        return try {
            val responseText = conn.inputStream.bufferedReader().readText()
            conn.disconnect()

            val json = JSONObject(responseText)
            val candidates = json.optJSONArray("candidates")

            if (candidates == null || candidates.length() == 0) {
                throw Exception("لا توجد نتائج")
            }

            val content = candidates.getJSONObject(0)
                .optJSONObject("content")

            if (content == null) {
                throw Exception("محتوى فارغ")
            }

            val parts = content.optJSONArray("parts")

            if (parts == null || parts.length() == 0) {
                throw Exception("أجزاء فارغة")
            }

            parts.getJSONObject(0).getString("text").trim()

        } catch (e: Exception) {
            if (e.message?.contains("text") == true &&
                e.message?.contains("Empty") == true
            ) {
                throw Exception("استجابة فارغة")
            }
            throw e
        }
    }

    // ═══════════════════════════════════════════════════════════
    // ═══ OpenAI ═══
    // ═══════════════════════════════════════════════════════════
    private fun callOpenAI(prompt: String): String {
        val url = URL("https://api.openai.com/v1/chat/completions")
        val conn = url.openConnection() as HttpURLConnection
        conn.apply {
            requestMethod = "POST"
            setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            setRequestProperty("Authorization", "Bearer ${settings.openaiKey.trim()}")
            connectTimeout = 60000
            readTimeout = 60000
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

        OutputStreamWriter(conn.outputStream, Charsets.UTF_8).use {
            it.write(body.toString())
            it.flush()
        }

        if (conn.responseCode != 200) {
            throw Exception(getErrorMessage(conn))
        }

        val response = conn.inputStream.bufferedReader().readText()
        conn.disconnect()

        val json = JSONObject(response)
        return json.getJSONArray("choices")
            .getJSONObject(0)
            .getJSONObject("message")
            .getString("content")
            .trim()
    }

    // ═══════════════════════════════════════════════════════════
    // ═══ Mistral ═══
    // ═══════════════════════════════════════════════════════════
    private fun callMistral(prompt: String): String {
        val url = URL("https://api.mistral.ai/v1/chat/completions")
        val conn = url.openConnection() as HttpURLConnection
        conn.apply {
            requestMethod = "POST"
            setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            setRequestProperty("Authorization", "Bearer ${settings.mistralKey.trim()}")
            connectTimeout = 60000
            readTimeout = 60000
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

        OutputStreamWriter(conn.outputStream, Charsets.UTF_8).use {
            it.write(body.toString())
            it.flush()
        }

        if (conn.responseCode != 200) {
            throw Exception(getErrorMessage(conn))
        }

        val response = conn.inputStream.bufferedReader().readText()
        conn.disconnect()

        val json = JSONObject(response)
        return json.getJSONArray("choices")
            .getJSONObject(0)
            .getJSONObject("message")
            .getString("content")
            .trim()
    }

    // ═══════════════════════════════════════════════════════════
    // ═══ Custom ═══
    // ═══════════════════════════════════════════════════════════
    private fun callCustom(prompt: String): String {
        val baseUrl = settings.customUrl.trim().trimEnd('/')
        val url = URL("$baseUrl/v1/chat/completions")
        val conn = url.openConnection() as HttpURLConnection
        conn.apply {
            requestMethod = "POST"
            setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            setRequestProperty("Authorization", "Bearer ${settings.customKey.trim()}")
            connectTimeout = 60000
            readTimeout = 60000
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

        OutputStreamWriter(conn.outputStream, Charsets.UTF_8).use {
            it.write(body.toString())
            it.flush()
        }

        if (conn.responseCode != 200) {
            throw Exception(getErrorMessage(conn))
        }

        val response = conn.inputStream.bufferedReader().readText()
        conn.disconnect()

        val json = JSONObject(response)
        return json.getJSONArray("choices")
            .getJSONObject(0)
            .getJSONObject("message")
            .getString("content")
            .trim()
    }
}
