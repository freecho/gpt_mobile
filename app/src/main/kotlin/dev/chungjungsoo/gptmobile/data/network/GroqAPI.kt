package dev.chungjungsoo.gptmobile.data.network

import dev.chungjungsoo.gptmobile.data.dto.groq.request.GroqChatCompletionRequest
import dev.chungjungsoo.gptmobile.data.dto.groq.response.GroqChatCompletionChunk
import kotlinx.coroutines.flow.Flow

interface GroqAPI {
    fun setToken(token: String?)
    fun setAPIUrl(url: String)
    fun streamChatCompletion(request: GroqChatCompletionRequest, timeoutSeconds: Int): Flow<GroqChatCompletionChunk>
}
