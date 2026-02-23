package com.example.airich.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface DeepSeekApi {
    @POST("api/chat")
    suspend fun createChatCompletion(
        @Body request: DeepSeekRequest
    ): Response<DeepSeekResponse>
}

