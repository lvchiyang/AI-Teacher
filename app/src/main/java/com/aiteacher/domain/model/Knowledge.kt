package com.aiteacher.domain.model

/**
 * 知识点领域模型
 * 对应知识库表(KnowledgeBase)
 */
data class Knowledge(
    val knowledgeId: String,           // 知识点ID，如 "M7-001"
    val subject: String,               // 学科，如 "数学"
    val grade: Int,                    // 年级，如 7
    val chapter: String,               // 章节，如 "有理数"
    val concept: String,               // 概念描述
    val applicationMethods: List<String>, // 应用方法列表
    val keywords: List<String>         // 关键词列表
)