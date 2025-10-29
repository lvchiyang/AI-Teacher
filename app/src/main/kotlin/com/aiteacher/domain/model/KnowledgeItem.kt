package com.aiteacher.domain.model

/**
 * 知识点关联项领域模型
 */
data class KnowledgeItem(
    val knowledgeId: String,
    val topic: String,
    val subject: String
)