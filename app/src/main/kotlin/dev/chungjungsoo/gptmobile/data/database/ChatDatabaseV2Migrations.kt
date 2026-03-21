package dev.chungjungsoo.gptmobile.data.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object ChatDatabaseV2Migrations {

    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `chat_platform_model_v2` (
                    `chat_id` INTEGER NOT NULL,
                    `platform_uid` TEXT NOT NULL,
                    `model` TEXT NOT NULL,
                    `updated_at` INTEGER NOT NULL,
                    PRIMARY KEY(`chat_id`, `platform_uid`),
                    FOREIGN KEY(`chat_id`) REFERENCES `chats_v2`(`chat_id`) ON UPDATE NO ACTION ON DELETE CASCADE
                )
                """.trimIndent()
            )

            val platformModelMap = mutableMapOf<String, String>()
            db.query("SELECT uid, model FROM platform_v2").use { platformCursor ->
                val uidIndex = platformCursor.getColumnIndexOrThrow("uid")
                val modelIndex = platformCursor.getColumnIndexOrThrow("model")
                while (platformCursor.moveToNext()) {
                    val uid = platformCursor.getString(uidIndex)
                    val model = platformCursor.getString(modelIndex) ?: ""
                    platformModelMap[uid] = model
                }
            }

            val currentTimestamp = System.currentTimeMillis() / 1000
            db.query("SELECT chat_id, enabled_platform FROM chats_v2").use { chatCursor ->
                val chatIdIndex = chatCursor.getColumnIndexOrThrow("chat_id")
                val enabledPlatformIndex = chatCursor.getColumnIndexOrThrow("enabled_platform")
                while (chatCursor.moveToNext()) {
                    val chatId = chatCursor.getInt(chatIdIndex)
                    val enabledPlatform = chatCursor.getString(enabledPlatformIndex) ?: ""
                    if (enabledPlatform.isBlank()) continue

                    enabledPlatform
                        .split(",")
                        .map { it.trim() }
                        .filter { it.isNotBlank() }
                        .forEach { platformUid ->
                            val model = platformModelMap[platformUid] ?: ""
                            db.execSQL(
                                "INSERT OR REPLACE INTO chat_platform_model_v2 (chat_id, platform_uid, model, updated_at) VALUES (?, ?, ?, ?)",
                                arrayOf<Any>(chatId, platformUid, model, currentTimestamp)
                            )
                        }
                }
            }
        }
    }
}
