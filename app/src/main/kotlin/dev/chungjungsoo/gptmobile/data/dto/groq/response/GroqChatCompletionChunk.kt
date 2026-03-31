package dev.chungjungsoo.gptmobile.data.dto.groq.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GroqChatCompletionChunk(
    @SerialName("id")
    val id: String? = null,

    @SerialName("object")
    val objectType: String? = null,

    @SerialName("created")
    val created: Long? = null,

    @SerialName("model")
    val model: String? = null,

    @SerialName("choices")
    val choices: List<GroqChoice>? = null,

    @SerialName("error")
    val error: GroqErrorDetail? = null
)

@Serializable
data class GroqChoice(
    @SerialName("index")
    val index: Int,

    @SerialName("delta")
    val delta: GroqDelta? = null,

    @SerialName("message")
    val message: GroqMessage? = null,

    @SerialName("finish_reason")
    val finishReason: String? = null
)

@Serializable
data class GroqDelta(
    @SerialName("role")
    val role: String? = null,

    @SerialName("content")
    val content: String? = null,

    @SerialName("reasoning")
    val reasoning: String? = null
)

@Serializable
data class GroqMessage(
    @SerialName("role")
    val role: String? = null,

    @SerialName("content")
    val content: String? = null,

    @SerialName("reasoning")
    val reasoning: String? = null
)

@Serializable
data class GroqErrorDetail(
    @SerialName("message")
    val message: String,

    @SerialName("type")
    val type: String? = null,

    @SerialName("code")
    val code: String? = null
)
