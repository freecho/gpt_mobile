package dev.chungjungsoo.gptmobile.data.dto

sealed class ApiState {
    data object Loading : ApiState()
    data class Thinking(val thinkingChunk: String) : ApiState()
    data class Success(val textChunk: String) : ApiState()
    /** Emitted while an Anthropic tool (e.g. web search) is executing server-side. */
    data class ToolUsing(val toolName: String) : ApiState()
    data class Error(val message: String) : ApiState()
    data object Done : ApiState()
}
