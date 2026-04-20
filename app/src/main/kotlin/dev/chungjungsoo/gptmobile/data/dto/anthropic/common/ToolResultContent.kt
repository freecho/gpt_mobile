package dev.chungjungsoo.gptmobile.data.dto.anthropic.common

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Represents a `tool_result` block in the user message that follows a `tool_use`. */
@OptIn(ExperimentalSerializationApi::class)
@Serializable
@SerialName("tool_result")
data class ToolResultContent(
    @SerialName("tool_use_id")
    val toolUseId: String,

    /** Simple string result from the tool execution. */
    @SerialName("content")
    val content: String,

    @SerialName("is_error")
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    val isError: Boolean? = null
) : MessageContent()
