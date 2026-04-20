package dev.chungjungsoo.gptmobile.data.dto.anthropic.common

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a `thinking` block in an assistant message.
 * When using extended thinking with tool use, this block MUST be passed back to the API
 * in subsequent turns to maintain reasoning continuity.
 */
@OptIn(ExperimentalSerializationApi::class)
@Serializable
@SerialName("thinking")
data class ThinkingContent(
    @SerialName("thinking")
    val thinking: String,

    @SerialName("signature")
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    val signature: String? = null
) : MessageContent()
