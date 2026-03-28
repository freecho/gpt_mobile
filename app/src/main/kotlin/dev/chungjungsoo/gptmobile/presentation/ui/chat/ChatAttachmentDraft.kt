package dev.chungjungsoo.gptmobile.presentation.ui.chat

import java.io.File

data class ChatAttachmentDraft(
    val sourceFilePath: String,
    val preparedFilePath: String? = null,
    val mimeType: String = "",
    val status: Status = Status.Preparing,
    val notice: String? = null,
    val errorMessage: String? = null
) {
    val id: String = sourceFilePath
    val displayName: String = File(preparedFilePath ?: sourceFilePath).name

    enum class Status {
        Preparing,
        Ready,
        Failed
    }
}
