package dev.chungjungsoo.gptmobile.data.dto.anthropic.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class StopReason {

    @SerialName("end_turn")
    END_TURN,

    @SerialName("max_tokens")
    MAX_TOKENS,

    @SerialName("stop_sequence")
    STOP_SEQUENCE,

    @SerialName("tool_use")
    TOOL_USE,

    // Returned when the server-side agentic loop hits its iteration limit.
    // The client should re-send the conversation (user + assistant messages) without a
    // tool_result so the server can resume where it left off.
    @SerialName("pause_turn")
    PAUSE_TURN
}
