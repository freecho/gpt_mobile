package dev.chungjungsoo.gptmobile.data.dto.anthropic.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ContentBlock(

    @SerialName("type")
    val type: ContentBlockType,

    @SerialName("text")
    val text: String? = null,

    @SerialName("thinking")
    val thinking: String? = null,

    // Signature delta field (signature_delta type)
    @SerialName("signature")
    val signature: String? = null,

    // tool_use block fields
    @SerialName("id")
    val id: String? = null,

    @SerialName("name")
    val name: String? = null,

    // Streamed tool input JSON fragments (input_json_delta)
    @SerialName("partial_json")
    val partialJson: String? = null
)
