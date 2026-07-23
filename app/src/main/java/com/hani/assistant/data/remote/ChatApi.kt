package com.hani.assistant.data.remote

import retrofit2.http.*
import retrofit2.Call

interface ChatApi {
    @POST("v1/chat/completions")
    @Headers("Content-Type: application/json")
    fun getChatCompletion(
        @Header("Authorization") auth: String,
        @Body request: ChatRequest
    ): Call<ChatResponse>
}

data class ChatRequest(
    val model: String = "gpt-4o-mini",
    val messages: List<ChatMessage>,
    val temperature: Double = 0.9,
    val max_tokens: Int = 150
)

data class ChatMessage(
    val role: String, // "system", "user", "assistant"
    val content: String
)

data class ChatResponse(
    val choices: List<Choice>
)

data class Choice(
    val message: ChatMessage
)