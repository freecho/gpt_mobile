package dev.chungjungsoo.gptmobile.data.dto.anthropic.common

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/** Represents a `tool_use` block inside an assistant message sent back to the API. */
@Serializable
@SerialName("tool_use")
data class ToolUseContent(
    @SerialName("id")
    val id: String,

    @SerialName("name")
    val name: String,

    @SerialName("input")
    val input: JsonObject
) : MessageContent()
