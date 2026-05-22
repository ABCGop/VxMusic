package org.simpmusic.aiservice

import com.aallam.openai.api.chat.ChatCompletion
import com.aallam.openai.api.chat.ChatResponseFormat
import com.aallam.openai.api.chat.JsonSchema
import com.aallam.openai.api.chat.chatCompletionRequest
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import com.aallam.openai.client.OpenAIConfig
import com.aallam.openai.client.OpenAIHost
import com.aallam.openai.client.OpenAIHost.Companion.Gemini
import com.maxrave.domain.data.model.metadata.Line
import com.maxrave.domain.data.model.metadata.Lyrics
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject

class AiService(
    private val aiHost: AIHost = AIHost.GEMINI,
    private val apiKey: String,
    private val customModelId: String? = null,
    private val customBaseUrl: String? = null,
    private val customHeaders: Map<String, String>? = null,
) {
    private val json =
        Json {
            ignoreUnknownKeys = true
            isLenient = true
            explicitNulls = false
        }
    private val openAI: OpenAI by lazy {
        when (aiHost) {
            AIHost.GEMINI -> {
                OpenAI(host = Gemini, token = apiKey)
            }

            AIHost.OPENAI -> {
                OpenAI(token = apiKey)
            }

            AIHost.CUSTOM_OPENAI -> {
                val baseUrl = customBaseUrl ?: "https://api.openai.com/v1/"
                val config =
                    OpenAIConfig(
                        token = apiKey,
                        host = OpenAIHost(baseUrl = baseUrl),
                        headers = customHeaders ?: emptyMap(),
                    )
                OpenAI(config)
            }
        }
    }

    private val model by lazy {
        if (!customModelId.isNullOrEmpty()) {
            ModelId(customModelId)
        } else {
            when (aiHost) {
                AIHost.GEMINI -> ModelId("gemini-2.0-flash")
                AIHost.OPENAI -> ModelId("gpt-4o")
                AIHost.CUSTOM_OPENAI -> ModelId("gpt-4o")
            }
        }
    }

    suspend fun translateLyrics(
        inputLyrics: Lyrics,
        targetLanguage: String,
    ): Lyrics {
        val lines = inputLyrics.lines ?: throw IllegalStateException("No lyrics lines to translate")

        // Build key-value map: index -> words (only non-empty lines)
        val indexToWords = mutableMapOf<String, String>()
        lines.forEachIndexed { index, line ->
            val words = line.words.trim()
            if (words.isNotEmpty() && words != "♫") {
                indexToWords[index.toString()] = words
            }
        }

        if (indexToWords.isEmpty()) {
            throw IllegalStateException("No translatable lyrics lines found")
        }

        val inputJson = json.encodeToString(MapSerializer(String.serializer(), String.serializer()), indexToWords)

        val request =
            chatCompletionRequest {
                this.model = this@AiService.model
                responseFormat = ChatResponseFormat.jsonSchema(aiResponseJsonSchema)
                messages {
                    system {
                        content =
                            "You are a song lyrics translation assistant.\n" +
                            "\n" +
                            "TASK:\n" +
                            "- You will receive a JSON object where keys are line indices and values are lyrics text.\n" +
                            "- Translate ONLY the values to the target language.\n" +
                            "- Keep ALL keys exactly the same.\n" +
                            "- The output MUST have the EXACT same number of entries as the input.\n" +
                            "- Do NOT merge, split, add, or remove any entries.\n" +
                            "- Preserve the song's meaning, tone, and emotion in the translation.\n" +
                            "\n" +
                            "OUTPUT:\n" +
                            "- A JSON object with the \"translations\" field containing the same keys mapped to translated values."
                    }
                    user {
                        content {
                            text("Target language: $targetLanguage")
                        }
                        content {
                            text("Input lyrics: $inputJson")
                        }
                    }
                }
            }
        val completion: ChatCompletion = openAI.chatCompletion(request)
        val jsonContent =
            completion.choices
                .firstOrNull()
                ?.message
                ?.content ?: throw IllegalStateException("No response from AI")
        val jsonData =
            Regex(
                "```json\\s*([\\s\\S]*?)```",
            ).find(jsonContent)
                ?.groups
                ?.firstOrNull()
                ?.value ?: jsonContent
        val cleanedJson = jsonData.replace("```json", "").replace("```", "")
        val translationResponse = json.decodeFromString<TranslationResponse>(cleanedJson)
        val translatedMap = translationResponse.translations

        // Map translated text back to original lines, preserving all timestamps
        val translatedLines = lines.mapIndexed { index, originalLine ->
            val translatedWords = translatedMap[index.toString()]
            if (translatedWords != null) {
                Line(
                    startTimeMs = originalLine.startTimeMs,
                    endTimeMs = originalLine.endTimeMs,
                    words = translatedWords,
                    syllables = null,
                )
            } else {
                // Non-translatable line (empty or ♫): keep original
                Line(
                    startTimeMs = originalLine.startTimeMs,
                    endTimeMs = originalLine.endTimeMs,
                    words = originalLine.words,
                    syllables = originalLine.syllables,
                )
            }
        }

        return Lyrics(
            error = false,
            lines = translatedLines,
            syncType = inputLyrics.syncType,
        )
    }

    companion object {
        private val translationJsonSchema: JsonObject =
            buildJsonObject {
                put("type", "object")
                putJsonObject("properties") {
                    putJsonObject("translations") {
                        put("type", "object")
                        putJsonObject("additionalProperties") {
                            put("type", "string")
                        }
                    }
                }
                putJsonArray("required") {
                    add("translations")
                }
            }
        private val aiResponseJsonSchema =
            JsonSchema(
                name = "ai_translation_schema",
                schema = translationJsonSchema,
                strict = false,
            )
    }
}

@kotlinx.serialization.Serializable
data class TranslationResponse(
    val translations: Map<String, String>,
)

enum class AIHost {
    GEMINI,
    OPENAI,
    CUSTOM_OPENAI,
}