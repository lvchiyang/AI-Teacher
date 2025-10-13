package com.aiteacher.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * 教学计划模型
 * 教秘Agent制定的教学计划
 */
@Parcelize
data class TeachingPlan(
    val planId: String,                       // 计划ID
    val studentId: String,                    // 学生ID
    val date: String,                         // 日期
    val grade: Int,                           // 年级
    val currentChapter: String,                // 当前章节
    val reviewKnowledgePoints: List<String>,   // 需要复习的知识点ID列表
    val newKnowledgePoints: List<String>,     // 新教知识点ID列表
    val estimatedDuration: Int,               // 预计时长（分钟）
    val status: PlanStatus                     // 计划状态
) : Parcelable

/**
 * 计划状态枚举
 */
enum class PlanStatus {
    PENDING,        // 待执行
    IN_PROGRESS,    // 进行中
    COMPLETED,      // 已完成
    CANCELLED       // 已取消
}
