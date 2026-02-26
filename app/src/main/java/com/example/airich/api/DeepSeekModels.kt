package com.example.airich.api

import com.google.gson.annotations.SerializedName

data class DeepSeekRequest(
    @SerializedName("model")
    val model: String = "deepseek-chat",

    @SerializedName("messages")
    val messages: List<DeepSeekMessage>,

    @SerializedName("temperature")
    val temperature: Double = 0.7,

    @SerializedName("max_tokens")
    val maxTokens: Int = 2000,

    @SerializedName("stream")
    val stream: Boolean = false
)

data class DeepSeekMessage(
    @SerializedName("role")
    val role: String,

    @SerializedName("content")
    val content: String
)

data class DeepSeekResponse(
    @SerializedName("id")
    val id: String?,

    @SerializedName("object")
    val objectType: String?,

    @SerializedName("created")
    val created: Long?,

    @SerializedName("model")
    val model: String?,

    @SerializedName("choices")
    val choices: List<DeepSeekChoice>?,

    @SerializedName("usage")
    val usage: DeepSeekUsage?,

    @SerializedName("error")
    val error: DeepSeekError?
)

data class DeepSeekChoice(
    @SerializedName("index")
    val index: Int?,

    @SerializedName("message")
    val message: DeepSeekMessage?,

    @SerializedName("finish_reason")
    val finishReason: String?
)

data class DeepSeekUsage(
    @SerializedName("prompt_tokens")
    val promptTokens: Int?,

    @SerializedName("completion_tokens")
    val completionTokens: Int?,

    @SerializedName("total_tokens")
    val totalTokens: Int?
)

data class DeepSeekError(
    @SerializedName("message")
    val message: String?,

    @SerializedName("type")
    val type: String?,

    @SerializedName("code")
    val code: String?
)
