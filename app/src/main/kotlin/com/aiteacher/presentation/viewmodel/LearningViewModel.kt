package com.aiteacher.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aiteacher.domain.model.TeachingTask
import com.aiteacher.domain.model.TestingTask
import com.aiteacher.domain.model.Question
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 学习界面ViewModel
 * 管理教学和检验阶段的状态
 */
class LearningViewModel : ViewModel() {
    
    // 反馈信息
    private val _feedback = MutableStateFlow<String?>(null)
    val feedback: StateFlow<String?> = _feedback.asStateFlow()
    
    // 当前教学任务（示例数据）
    val currentTeachingTask: TeachingTask = TeachingTask(
        taskId = "task_1",
        planId = "plan_1",
        day = 1,
        date = "2024-01-01",
        title = "示例教学任务",
        description = "讲解1+1的加法运算",
        topics = emptyList(),
        relatedKnowledge = emptyList(),
        estimatedTime = 30,
        content = "",
        resources = emptyList(),
        completed = false,
        completionDate = null,
        grade = 0,
        maxGrade = 0,
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis(),
        noResponseCount = 0
    )
    
    // 当前检验任务（示例数据）
    val currentTestingTask: TestingTask = TestingTask(
        taskId = "test_1",
        studentId = "student_1",
        title = "小测验",
        description = "基础计算",
        questionIds = listOf("q_1"),
        questions = listOf(
            Question(
                questionId = "q_1",
                subject = "",
                grade = 0,
                questionText = "测试题目：1 + 1 = ?",
                answer = "2",
                questionType = "calc",
                difficulty = null,
                relatedKnowledgeIds = emptyList()
            )
        ),
        totalScore = 0,
        passingScore = 0,
        timeLimit = 30,
        startedAt = System.currentTimeMillis(),
        completedAt = null,
        score = null,
        completed = false,
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis()
    )
    
    /**
     * 提交教学答案
     */
    fun submitTeachingAnswer(answer: String) {
        // 简单判断答案是否正确
        val isCorrect = answer == "2"
        _feedback.value = if (isCorrect) {
            "回答正确！可以进入检验阶段了。"
        } else {
            "回答错误，正确答案是2"
        }
    }
    
    /**
     * 提交检验答案
     */
    fun submitTestingAnswer(answer: String) {
        // 简单判断答案是否正确
        val isCorrect = answer == "2"
        _feedback.value = if (isCorrect) {
            "回答正确！"
        } else {
            "回答错误，正确答案是2"
        }
    }
    
    /**
     * 清空反馈
     */
    fun clearFeedback() {
        _feedback.value = null
    }
}
