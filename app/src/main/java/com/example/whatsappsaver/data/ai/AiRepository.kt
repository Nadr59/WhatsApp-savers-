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

            // ═══ كل المزودين المدعومين ═══
            val result = when (settings.provider) {
                "groq" -> callGroq(prompt)
                "openrouter" -> callOpenRouter(prompt)
                "openai" -> callOpenAI(prompt)
                "gemini" -> callGemini(prompt)
                "mistral" -> callMistral(prompt)
                "custom" -> callCustom(prompt)
                else -> return@withContext Result.failure(
                    Exception("مزود غير معروف: ${settings.provider}")
                )
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
    // ═══ Groq — مجاني تماماً ═══
    // ═══════════════════════════════════════════════════════════
    private fun callGroq(prompt: String): String {
        val apiKey = settings.groqKey.trim()
        val model = settings.groqModel.trim()

        if (apiKey.isBlank()) {
            throw Exception("مفتاح Groq فارغ. أنشئ مفتاح مجاني من:\nconsole.groq.com/keys")
        }

        val modelsToTry = mutableListOf<String>()
        modelsToTry.add(model)

        val fallbacks = listOf(
            "llama-3.1-8b-instant",
            "llama-3.3-70b-versatile",
            "gemma2-9b-it",
            "mixtral-8x7b-32768"
        )
        for (fb in fallbacks) {
            if (fb !in modelsToTry) modelsToTry.add(fb)
        }

        val errors = mutableListOf<String>()

        for (m in modelsToTry) {
            try {
                val result = callOpenAICompatible(
                    url = "https://api.groq.com/openai/v1/chat/completions",
                    apiKey = apiKey,
                    model = m,
                    prompt = prompt,
                    extraHeaders = emptyMap()
                )
                if (result.isNotBlank()) return result
            } catch (e: Exception) {
                errors.add("$m: ${e.message?.take(100)}")
            }
        }

        throw Exception("فشل Groq:\n${errors.joinToString("\n") { "  - $it" }}")
    }

    // ═══════════════════════════════════════════════════════════
    // ═══ OpenRouter ═══
    // ═══════════════════════════════════════════════════════════
    private fun callOpenRouter(prompt: String): String {
        val apiKey = settings.openrouterKey.trim()
        val model = settings.openrouterModel.trim()

        if (apiKey.isBlank()) {
            throw Exception("مفتاح OpenRouter فارغ")
        }

        val cleanModel = model.removeSuffix(":free").trim()

        val modelsToTry = mutableListOf<String>()
        if (cleanModel.isNotBlank()) modelsToTry.add(cleanModel)

        val fallbacks = listOf(
            "google/gemini-2.0-flash-exp",
            "meta-llama/llama-3.1-8b-instruct",
            "mistralai/mistral-7b-instruct",
            "deepseek/deepseek-chat-v3-0324",
            "openai/gpt-3.5-turbo"
        )
        for (fb in fallbacks) {
            if (fb !in modelsToTry) modelsToTry.add(fb)
        }

        val errors = mutableListOf<String>()

        for (m in modelsToTry) {
            try {
                val result = callOpenAICompatible(
                    url = "https://openrouter.ai/api/v1/chat/completions",
                    apiKey = apiKey,
                    model = m,
                    prompt = prompt,
                    extraHeaders = mapOf(
                        "HTTP-Referer" to "https://whatsapp-saver.app",
                        "X-Title" to "WhatsApp Saver"
                    )
                )
                if (result.isNotBlank()) return result
            } catch (e: Exception) {
                errors.add("$m: ${e.message?.take(100)}")
            }
        }

        throw Exception("فشل OpenRouter:\n${errors.joinToString("\n") { "  - $it" }}")
    }

    // ═══════════════════════════════════════════════════════════
    // ═══ OpenAI ═══
    // ═══════════════════════════════════════════════════════════
    private fun callOpenAI(prompt: String): String {
        return callOpenAICompatible(
            url = "https://api.openai.com/v1/chat/completions",
            apiKey = settings.openaiKey.trim(),
            model = settings.customModel,
            prompt = prompt,
            extraHeaders = emptyMap()
        )
    }

    // ═══════════════════════════════════════════════════════════
    // ═══ Gemini ═══
    // ═══════════════════════════════════════════════════════════
    private fun callGemini(prompt: String): String {
        val apiKey = settings.geminiKey.trim()
        if (apiKey.isBlank()) throw Exception("مفتاح Gemini فارغ")

        val models = listOf("gemini-2.0-flash", "gemini-1.5-flash", "gemini-1.5-pro")
        val errors = mutableListOf<String>()

        for (model in models) {
            try {
                val result = callGeminiModel(apiKey, model, prompt)
                if (result.isNotBlank()) return result
            } catch (e: Exception) {
                errors.add("$model: ${e.message?.take(100)}")
            }
        }

        throw Exception("فشل Gemini:\n${errors.joinToString("\n") { "  - $it" }}")
    }

    private fun callGeminiModel(apiKey: String, model: String, prompt: String): String {
        val url = URL("https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey")
        val conn = url.openConnection() as HttpURLConnection
        conn.apply {
            requestMethod = "POST"
            setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            connectTimeout = 60000
            readTimeout = 60000
            doOutput = true
        }

        val body = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "user")
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", prompt) })
                    })
                })
            })
            put("generationConfig", JSONObject().apply {
                put("temperature", 0.7)
                put("maxOutputTokens", 2048)
            })
        }

        OutputStreamWriter(conn.outputStream, Charsets.UTF_8).use {
            it.write(body.toString())
            it.flush()
        }

        if (conn.responseCode != 200) {
            val msg = getErrorMessage(conn)
            conn.disconnect()
            throw Exception(msg)
        }

        val response = conn.inputStream.bufferedReader().readText()
        conn.disconnect()

        val json = JSONObject(response)
        val candidates = json.optJSONArray("candidates")
        if (candidates != null && candidates.length() > 0) {
            val parts = candidates.getJSONObject(0)
                .optJSONObject("content")
                ?.optJSONArray("parts")
            if (parts != null && parts.length() > 0) {
                return parts.getJSONObject(0).getString("text").trim()
            }
        }
        throw Exception("استجابة فارغة")
    }

    // ═══════════════════════════════════════════════════════════
    // ═══ Mistral ═══
    // ═══════════════════════════════════════════════════════════
    private fun callMistral(prompt: String): String {
        return callOpenAICompatible(
            url = "https://api.mistral.ai/v1/chat/completions",
            apiKey = settings.mistralKey.trim(),
            model = "mistral-tiny",
            prompt = prompt,
            extraHeaders = emptyMap()
        )
    }

    // ═══════════════════════════════════════════════════════════
    // ═══ Custom ═══
    // ═══════════════════════════════════════════════════════════
    private fun callCustom(prompt: String): String {
        val baseUrl = settings.customUrl.trim().trimEnd('/')
        return callOpenAICompatible(
            url = "$baseUrl/v1/chat/completions",
            apiKey = settings.customKey.trim(),
            model = settings.customModel,
            prompt = prompt,
            extraHeaders = emptyMap()
        )
    }

    // ═══════════════════════════════════════════════════════════
    // ═══ دالة موحدة لجميع APIs المتوافقة مع OpenAI ═══
    // ═══════════════════════════════════════════════════════════
    private fun callOpenAICompatible(
        url: String,
        apiKey: String,
        model: String,
        prompt: String,
        extraHeaders: Map<String, String>
    ): String {
        val urlObj = URL(url)
        val conn = urlObj.openConnection() as HttpURLConnection
        conn.apply {
            requestMethod = "POST"
            setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            setRequestProperty("Authorization", "Bearer $apiKey")
            extraHeaders.forEach { (k, v) -> setRequestProperty(k, v) }
            connectTimeout = 60000
            readTimeout = 60000
            doOutput = true
            doInput = true
        }

        val body = JSONObject().apply {
            put("model", model)
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
            val errorMsg = getErrorMessage(conn)
            conn.disconnect()
            throw Exception(errorMsg)
        }

        val response = conn.inputStream.bufferedReader().readText()
        conn.disconnect()

        val json = JSONObject(response)
        val choices = json.optJSONArray("choices")
        if (choices != null && choices.length() > 0) {
            val content = choices.getJSONObject(0)
                .optJSONObject("message")
                ?.optString("content", "")
            if (!content.isNullOrBlank()) {
                return content.trim()
            }
        }
        throw Exception("استجابة فارغة")
    }
}
