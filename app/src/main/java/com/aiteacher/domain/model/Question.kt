package com.aiteacher.domain.model

/**
 * 题目领域模型
 * 对应题库表(QuestionBase)
 */
data class Question(
    val questionId: String,             // 题目ID，如 "Q1001"
    val subject: String,               // 学科，如 "数学"
    val grade: Int,                    // 年级，如 7
    val questionText: String,          // 题目内容
    val answer: String,                // 答案
    val questionType: String,          // 题目类型，单选题/多选题/判断题/填空题/简答题
    val difficulty: Int?,               // 难度等级
    val relatedKnowledgeIds: List<String> // 关联的知识点ID列表
)