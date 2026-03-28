package dev.chungjungsoo.gptmobile.util

import java.util.concurrent.ConcurrentHashMap

object AttachmentPayloadCache {
    private val payloads = ConcurrentHashMap<String, FileUtils.EncodedImage>()

    fun get(filePath: String): FileUtils.EncodedImage? = payloads[filePath]

    fun put(filePath: String, payload: FileUtils.EncodedImage) {
        payloads[filePath] = payload
    }

    fun remove(filePath: String) {
        payloads.remove(filePath)
    }
}
