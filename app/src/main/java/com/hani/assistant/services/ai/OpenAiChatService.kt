package com.hani.assistant.services.ai

import com.hani.assistant.BuildConfig
import com.hani.assistant.data.remote.ChatApi
import com.hani.assistant.data.remote.ChatMessage
import com.hani.assistant.data.remote.ChatRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OpenAiChatService @Inject constructor(
    private val api: ChatApi
) {

    suspend fun getChatResponse(userMessage: String, history: List<Pair<String, String>> = emptyList()): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val messages = mutableListOf(
                    ChatMessage(
                        role = "system",
                        content = "You are Hani, a cute anime girl assistant. Respond warmly, briefly, and conversationally. Never mention being an AI unless asked."
                    )
                )
                // Include recent conversation history (last 5 exchanges)
                history.takeLast(5).forEach { (user, bot) ->
                    messages.add(ChatMessage("user", user))
                    messages.add(ChatMessage("assistant", bot))
                }
                messages.add(ChatMessage("user", userMessage))

                val request = ChatRequest(messages = messages)
                val auth = "Bearer ${BuildConfig.OPENAI_API_KEY}"

                val response = api.getChatCompletion(auth, request).execute()

                if (!response.isSuccessful) {
                    throw HttpException(response)
                }

                val body = response.body()
                val content = body?.choices?.firstOrNull()?.message?.content
                    ?: "I'm not sure how to respond."

                Result.success(content)
            } catch (e: HttpException) {
                Result.failure(Exception("API error: ${e.message()}"))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
