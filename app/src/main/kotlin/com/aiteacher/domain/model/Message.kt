package com.aiteacher.domain.model

/**
 * 消息领域模型
 */
data class Message(
    val messageId: String,
    val sessionId: String,
    val role: String,
    val content: String,
    val tokens: Int,
    val createdAt: Long,
    val metadata: Map<String, String> = emptyMap()
)