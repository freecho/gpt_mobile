# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

GPT Mobile is an Android chat application that supports chatting with multiple AI models simultaneously (OpenAI GPT, Anthropic Claude, Google Gemini, Groq, and Ollama). Built with 100% Kotlin, Jetpack Compose, and follows Modern Android App Architecture patterns.

## Build Commands

### Development Build
```bash
./gradlew assembleDebug
```

### Release Build
```bash
./gradlew assembleRelease
./gradlew bundleRelease  # For Google Play Store AAB format
```

### Testing
```bash
./gradlew test                      # Unit tests
./gradlew connectedAndroidTest      # Instrumented tests
```

### Code Quality
- **Linting**: Handled by GitHub Actions with ktlint 1.3.1
- **Security**: CodeQL analysis runs automatically on main branch changes

## Architecture

### Core Structure
- **MVVM Pattern**: ViewModels handle UI state, Repositories manage data
- **Dependency Injection**: Hilt for all dependency management
- **Database**: Room for local chat history storage
- **Networking**: Ktor client with CIO engine for API calls
- **UI**: Jetpack Compose with Material 3 design system

### Data Flow
1. **Chat Flow**: `ChatViewModel` → `ChatRepository` → API clients (OpenAI, Anthropic, etc.) → Streaming responses
2. **Persistence**: All chat data stored locally via Room database
3. **Settings**: DataStore for user preferences and API configurations

### Key Components
- **ChatRepositoryImpl**: Handles all AI platform communications with streaming support
- **NetworkModule**: Configures Ktor client and Anthropic API
- **Platform Support**: Each AI service has dedicated completion methods with consistent `Flow<ApiState>` responses
- **Message Transformation**: Platform-specific message format conversion for API compatibility

### Module Structure
- `data/`: Models, DTOs, database entities, repositories, network clients
- `di/`: Hilt dependency injection modules
- `presentation/`: ViewModels, UI components, navigation, theming
- `util/`: Extensions, utilities, string resources

## Development Notes

- **Min SDK**: 31 (Android 12)
- **Target SDK**: 35
- **Java Version**: 17
- **Build System**: Gradle with Kotlin DSL
- **Material You**: Dynamic theming support without activity restart
- **Internationalization**: Multiple language support via string resources

---

## Anthropic / Claude Integration (详细)

### 当前实现

**网络层** (`data/network/`)
- `AnthropicAPI` — 接口，定义三个方法：`setToken`、`setAPIUrl`、`streamChatMessage`、`uploadFile`、`isFileAvailable`
- `AnthropicAPIImpl` — Ktor 实现，使用 SSE (`text/event-stream`) 流式接收响应，固定 header：
  - `x-api-key`、`anthropic-version: 2023-06-01`、`anthropic-beta: files-api-2025-04-14`
- 端点：`POST /v1/messages`（流式聊天）、`POST /v1/files`（文件上传）、`GET /v1/files/{id}`（文件可用性）

**请求 DTOs** (`data/dto/anthropic/request/`)
- `MessageRequest` — 顶层请求体，字段包括 `model`、`messages`、`max_tokens`、`stream`、`system`、`temperature`、`top_p`、`top_k`、`thinking`（扩展 extended thinking）
- `InputMessage` — 单条消息，包含 `role`（USER/ASSISTANT）和 `content: List<MessageContent>`
- `ThinkingConfig` — extended thinking 配置：`type="enabled"`、`budget_tokens`

**消息内容类型** (`data/dto/anthropic/common/`)
- `MessageContent`（sealed class）← `TextContent`（`@SerialName("text")`）、`ImageContent`（`@SerialName("image")`）
- `ContentType` enum：`TEXT`、`IMAGE`
- `ImageSource` — 支持 base64 和文件引用（`file` 类型）两种方式

**响应 DTOs** (`data/dto/anthropic/response/`)
- `MessageResponseChunk`（sealed class）— 所有 SSE 事件的基类
  - `MessageStartResponseChunk`、`ContentStartResponseChunk`、`ContentDeltaResponseChunk`、`ContentStopResponseChunk`、`MessageDeltaResponseChunk`、`MessageStopResponseChunk`、`PingResponseChunk`、`ErrorResponseChunk`
- `ContentBlock` — delta/block 数据，字段：`type: ContentBlockType`、`text`、`thinking`
- `ContentBlockType` enum：`TEXT`、`DELTA`、`THINKING`、`THINKING_DELTA`、`SIGNATURE`、`SIGNATURE_DELTA`
- `StopReason` enum：`END_TURN`、`MAX_TOKENS`、`STOP_SEQUENCE`、**`TOOL_USE`**（已定义，尚未使用）
- `EventType` enum：`MESSAGE_START`、`CONTENT_START`、`CONTENT_DELTA`、`CONTENT_STOP`、`MESSAGE_DELTA`、`MESSAGE_STOP`、`PING`、`ERROR`

**仓库层** (`data/repository/ChatRepositoryImpl.kt`)
- `completeChatWithAnthropic()` — 构建 `MessageRequest`，调用 `anthropicAPI.streamChatMessage()`，将 `ContentDeltaResponseChunk` 映射为 `ApiState.Success`（文本）或 `ApiState.Thinking`（thinking delta）

### 目标功能：Anthropic Claude Tools（云端工具）

目标是为 Claude 实现 [tools 支持](https://docs.anthropic.com/en/docs/tool-use)，尤其是 Anthropic 官方提供的**云端内置工具（computer use / web search）**，无需用户自行托管工具服务端。

#### 已实现（2026-04-20）

**1. 新增 DTOs**

- `data/dto/anthropic/request/AnthropicTool.kt` — `sealed class AnthropicTool`，子类 `WebSearch`（type=`web_search_20250305`）和 `Custom`；`BuiltinTool` enum 定义 UI 展示的工具列表
- `data/dto/anthropic/common/ToolUseContent.kt` — `@SerialName("tool_use")` ← `MessageContent`
- `data/dto/anthropic/common/ToolResultContent.kt` — `@SerialName("tool_result")` ← `MessageContent`

**2. 扩展现有 DTOs**

- `MessageRequest` — 新增 `tools: List<AnthropicTool>?` 和 `toolChoice: ToolChoice?`；`ToolChoice` 类也定义在同文件
- `ContentBlockType` — 新增 `TOOL_USE`、`INPUT_JSON_DELTA`
- `ContentBlock` — 新增 `id`、`name`、`partialJson` 字段
- `ApiState` — 新增 `ToolUsing(toolName: String)`（UI 可展示"正在搜索…"状态）

**3. 数据库迁移（版本 3 → 4）**

- `ChatRoomV2` — 新增 `enabled_tools: String`（逗号分隔工具 ID，空字符串表示无工具）
- `ChatDatabaseV2Migrations.MIGRATION_3_4` — `ALTER TABLE chats_v2 ADD COLUMN enabled_tools TEXT NOT NULL DEFAULT ''`
- `ChatDatabaseV2` version → 4，`DatabaseModule` 注册迁移

**4. 仓库层**

- `ChatRepository` — 新增 `completeChatWithTools()` 和 `updateChatEnabledTools()` 接口方法
- `ChatRepositoryImpl.completeChatWithTools()` — 实现 agentic loop：
  1. 构建带 `tools` 的 `MessageRequest`
  2. 流式接收，累积 `tool_use` 块（通过 `INPUT_JSON_DELTA` 拼接 JSON）
  3. `stop_reason=tool_use` → 将 assistant 的 tool_use 块追加为 `InputMessage`，再追加空的 `tool_result` 用户消息（web_search 服务端自执行）
  4. 重复直到 `stop_reason=end_turn` 或达到最大迭代次数（10 次）
- `updateChatEnabledTools()` — 将工具集合持久化到 `ChatRoomV2.enabled_tools`

**5. UI 层**

- `presentation/ui/chat/AnthropicToolsSheet.kt` — `ModalBottomSheet`，列出所有 `BuiltinTool` 并提供 Switch 开关
- `ChatScreen.kt` — `ChatInputBox` 新增 `showToolsButton`/`enabledTools`/`onToolsClick` 参数；当聊天含 Anthropic 平台时显示工具按钮（`ic_tools.xml`），激活时图标变为 primary 色
- `ChatViewModel` — 新增 `_isToolsSheetOpen`、`_enabledTools` StateFlow；`openToolsSheet()`、`closeToolsSheet()`、`toggleTool()` 方法；`completeChat()` 和 `retryChat()` 均判断是否走 tools 路径；`fetchChatRoom()` 加载已保存的工具设置；save 时携带当前工具到 `enabledTools` 字段

#### 关键约束

- `StopReason.TOOL_USE` 已定义，可直接使用
- `MessageRequest` 使用 `@EncodeDefault(Mode.NEVER)` 策略，`tools`/`toolChoice` 为 null 时不序列化
- web_search 是 **Anthropic 服务端内置工具**，客户端无需执行搜索逻辑，`tool_result` 内容留空即可
- streaming 模式下 tool input 通过 `INPUT_JSON_DELTA` 分片到达，需用 `StringBuilder` 拼接后解析 JSON
- tools 按钮仅在聊天含 Anthropic 兼容平台时显示（通过 `hasAnthropicPlatform()` 检测）

**1. 请求 DTOs（新增）**

在 `data/dto/anthropic/request/` 下新增：
- `AnthropicTool` — 工具定义（`name`、`description`、`input_schema`）
- `AnthropicBuiltinTool` — Anthropic 内置云端工具（如 `web_search`），通过 `type="computer_20250124"` 等标识
- 在 `MessageRequest` 中新增 `tools: List<AnthropicTool>?` 和 `tool_choice` 字段

**2. 响应 DTOs（新增/扩展）**

在 `data/dto/anthropic/response/` 下：
- 扩展 `ContentBlockType`：新增 `TOOL_USE`、`TOOL_RESULT`、`INPUT_JSON_DELTA` 等
- 新增 `ToolUseContentBlock`（`id`、`name`、`input`）
- 新增 `ToolResultContent`（用于将工具结果回传给模型）
- 扩展 `ContentBlock`（或用独立子类）处理 tool_use 块流式 delta

**3. 消息内容类型（扩展）**

在 `data/dto/anthropic/common/` 下：
- `ToolUseContent`（`@SerialName("tool_use")`）← `MessageContent`
- `ToolResultContent`（`@SerialName("tool_result")`）← `MessageContent`

**4. 网络层（扩展）**

- `AnthropicAPIImpl.streamChatMessage()` 中处理 `stop_reason=tool_use` 的情况
- 需要在触发 tool_use 后，构建包含 `tool_result` 的第二轮请求（agentic loop）
- 如启用 web_search，需在 `anthropic-beta` header 中附加对应的 beta flag（目前已有 `files-api-2025-04-14`，需检查 web search 是否需要额外 beta）

**5. 仓库层（扩展）**

- `completeChatWithAnthropic()` 需改写为支持多轮 agentic loop：
  1. 发送带 `tools` 的请求
  2. 收到 `stop_reason=tool_use` 时，提取 tool_use 块
  3. 执行工具（web search 由 Anthropic 云端执行，客户端只需回传 tool_result）
  4. 将 tool_result 附加到 messages 列表，发起下一轮请求
  5. 直到 `stop_reason=end_turn` 为止
- 新增 `ApiState` 状态（可选）：`ApiState.ToolUse`，用于在 UI 展示"正在搜索…"等中间状态

**6. UI 层（可选）**

- 在 `PlatformV2` 设置中新增 tools 开关（`enableTools: Boolean`、`enabledTools: List<String>`）
- 在聊天界面展示 tool_use 中间步骤（如搜索关键词、搜索结果摘要）

#### 关键约束

- `StopReason.TOOL_USE` 已在 `StopReason.kt` 中定义，可直接使用
- `MessageRequest` 使用 `@EncodeDefault(Mode.NEVER)` 策略，新增字段遵循同样模式
- Anthropic web search 是**服务端执行**的内置工具，客户端无需实现搜索逻辑，只需正确传递 `tools` 列表并处理多轮 agentic loop
- streaming 模式下，tool_use 的 input JSON 通过 `input_json_delta` 流式到达，需拼接后解析