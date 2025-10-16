package com.aiteacher.domain.model

/**
 * 学生实体模型
 * 记录学生的学习进度，按掌握状态分类记录知识点
 */
data class Student(
    val studentId: String,                    // 学生ID
    val studentName: String,                  // 学生姓名
    val grade: Int,                           // 年级
    val currentChapter: String,               // 当前学习章节
    val learningProgress: LearningProgress    // 学习进度
)

/**
 * 学习进度模型
 * 按掌握状态分类记录知识点
 */
data class LearningProgress(
    val taughtToReview: List<String>,        // 已讲解待复习的知识点ID列表
    val notMastered: List<String>,            // 未掌握的知识点ID列表
    val basicMastery: List<String>,           // 初步掌握的知识点ID列表
    val fullMastery: List<String>,            // 熟练掌握的知识点ID列表
    val lastUpdateTime: String                // 最后更新时间
)

/**
 * 掌握状态枚举
 */
enum class MasteryStatus {
    NOT_TAUGHT,        // 未讲解
    TAUGHT_TO_REVIEW,  // 已讲解待复习
    NOT_MASTERED,      // 未掌握
    BASIC_MASTERY,     // 初步掌握
    FULL_MASTERY       // 熟练掌握
}