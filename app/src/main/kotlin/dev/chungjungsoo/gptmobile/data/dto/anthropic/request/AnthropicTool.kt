package dev.chungjungsoo.gptmobile.data.dto.anthropic.request

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/**
 * Anthropic built-in or user-defined tool.
 *
 * Each subclass is a standalone @Serializable data class — NOT using sealed class polymorphism —
 * because the API discriminates by the "type" field that is already a property on each subclass,
 * and AnthropicAPIImpl's Json instance has encodeDefaults = false (which would suppress fields
 * with default values when using sealed class auto-polymorphism).
 */
@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class AnthropicWebSearchTool(
    @SerialName("type")
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val type: String = "web_search_20260209",

    @SerialName("name")
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val name: String = "web_search",

    /** Maximum number of searches Claude can perform per request. */
    @SerialName("max_uses")
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    val maxUses: Int? = null,

    /** Only return results from these domains. */
    @SerialName("allowed_domains")
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    val allowedDomains: List<String>? = null,

    /** Never return results from these domains. */
    @SerialName("blocked_domains")
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    val blockedDomains: List<String>? = null
)

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class AnthropicCustomTool(
    @SerialName("name")
    val name: String,

    @SerialName("description")
    val description: String,

    @SerialName("input_schema")
    val inputSchema: JsonObject
)

/** Available built-in tool identifiers surfaced in the UI. */
enum class BuiltinTool(val id: String, val displayName: String) {
    WEB_SEARCH("web_search", "Web Search")
}
