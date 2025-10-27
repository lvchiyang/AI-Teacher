package com.aiteacher.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aiteacher.domain.model.TaskType
import com.aiteacher.domain.model.TaskStatus
import com.aiteacher.domain.model.TeachingTask
import com.aiteacher.domain.model.TeachingContent
import com.aiteacher.domain.model.TestingTask
import com.aiteacher.domain.model.TestQuestion
import com.aiteacher.domain.model.QuestionType
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
        studentId = "student_1",
        knowledgePointId = "kp_1",
        taskType = TaskType.TEACHING,
        content = TeachingContent(
            text = "这是一个示例教学任务，讲解1+1的加法运算。",
            images = emptyList(),
            audio = null,
            ppt = null
        ),
        questions = emptyList(),
        status = TaskStatus.IN_PROGRESS,
        currentQuestionIndex = 0,
        noResponseCount = 0
    )
    
    // 当前检验任务（示例数据）
    val currentTestingTask: TestingTask = TestingTask(
        taskId = "test_1",
        studentId = "student_1",
        knowledgePointIds = listOf("kp_1"),
        questions = listOf(
            TestQuestion(
                questionId = "q_1",
                knowledgePointId = "kp_1",
                content = "测试题目：1 + 1 = ?",
                image = null,
                type = QuestionType.CALCULATION,
                correctAnswer = "2",
                explanation = "1加1等于2",
                points = 10,
                timeLimit = 5
            )
        ),
        status = TaskStatus.IN_PROGRESS,
        currentQuestionIndex = 0,
        startTime = "2024-01-01T00:00:00",
        timeLimit = 30
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
