package dev.chungjungsoo.gptmobile.data.dto.anthropic.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class ContentBlockType {

    @SerialName("text")
    TEXT,

    @SerialName("text_delta")
    DELTA,

    @SerialName("thinking")
    THINKING,

    @SerialName("thinking_delta")
    THINKING_DELTA,

    @SerialName("signature")
    SIGNATURE,

    @SerialName("signature_delta")
    SIGNATURE_DELTA,

    @SerialName("tool_use")
    TOOL_USE,

    @SerialName("input_json_delta")
    INPUT_JSON_DELTA,

    // Server-side tool events emitted by Anthropic infrastructure (e.g. web_search_20260209).
    // The server executes the tool internally; the client only needs to display the final text.
    @SerialName("server_tool_use")
    SERVER_TOOL_USE,

    @SerialName("web_search_tool_result")
    WEB_SEARCH_TOOL_RESULT,

    @SerialName("web_fetch_tool_result")
    WEB_FETCH_TOOL_RESULT,

    @SerialName("server_tool_use_delta")
    SERVER_TOOL_USE_DELTA
}
