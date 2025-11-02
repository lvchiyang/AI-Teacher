package com.aiteacher.domain.model

/**
 * 掌握状态枚举
 * 表示学生对知识点的掌握程度
 */
enum class MasteryStatus {
    NOT_TAUGHT,        // 未讲解
    TAUGHT_TO_REVIEW,  // 已讲解待复习
    NOT_MASTERED,      // 未掌握
    BASIC_MASTERY,     // 初步掌握
    FULL_MASTERY       // 熟练掌握
}

