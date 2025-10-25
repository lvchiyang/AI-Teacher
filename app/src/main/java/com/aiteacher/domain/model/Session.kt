package com.aiteacher.domain.model

/**
 * 会话领域模型
 */
data class Session(
    val sessionId: String,
    val userId: String,
    val title: String,
    val createdAt: Long,
    val updatedAt: Long,
    val tags: List<String> = emptyList()
)