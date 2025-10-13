package com.aiteacher.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * 教学任务模型
 * 教学Agent执行的教学任务
 */
@Parcelize
data class TeachingTask(
    val taskId: String,                       // 任务ID
    val studentId: String,                    // 学生ID
    val knowledgePointId: String,             // 知识点ID
    val taskType: TaskType,                   // 任务类型
    val content: TeachingContent,             // 教学内容
    val questions: List<Question>,            // 问题列表
    val status: TaskStatus,                   // 任务状态
    val currentQuestionIndex: Int,            // 当前问题索引
    val noResponseCount: Int                  // 无响应次数
) : Parcelable

/**
 * 任务类型枚举
 */
enum class TaskType {
    REVIEW,         // 复习任务
    TEACHING        // 教学任务
}

/**
 * 任务状态枚举
 */
enum class TaskStatus {
    PENDING,        // 待开始
    IN_PROGRESS,    // 进行中
    COMPLETED,      // 已完成
    FAILED          // 失败
}

/**
 * 教学内容模型
 */
@Parcelize
data class TeachingContent(
    val text: String,                         // 文本内容
    val images: List<String>,                 // 图片URL列表
    val audio: String?,                       // 音频URL
    val ppt: String?                          // PPT URL
) : Parcelable

/**
 * 问题模型
 */
@Parcelize
data class Question(
    val questionId: String,                   // 问题ID
    val content: String,                      // 问题内容
    val type: QuestionType,                   // 问题类型
    val correctAnswer: String,                // 正确答案
    val explanation: String                   // 解释说明
) : Parcelable

/**
 * 问题类型枚举
 */
enum class QuestionType {
    MULTIPLE_CHOICE,   // 选择题
    FILL_BLANK,       // 填空题
    CALCULATION,      // 计算题
    EXPLANATION       // 解释题
}
