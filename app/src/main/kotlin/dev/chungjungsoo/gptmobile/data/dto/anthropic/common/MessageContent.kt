package dev.chungjungsoo.gptmobile.data.dto.anthropic.common

import kotlinx.serialization.Serializable

@Serializable
sealed class MessageContent

// Subclasses registered via @SerialName on each file:
// TextContent       → "text"
// ImageContent      → "image"
// ThinkingContent   → "thinking"
// ToolUseContent    → "tool_use"
// ToolResultContent → "tool_result"
