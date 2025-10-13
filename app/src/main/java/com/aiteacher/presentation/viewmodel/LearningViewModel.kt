package com.aiteacher.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aiteacher.domain.model.*
import com.aiteacher.domain.usecase.TeachingPlanUseCase
import com.aiteacher.domain.usecase.TeachingTaskUseCase
import com.aiteacher.domain.usecase.TestingTaskUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 学习ViewModel
 * 处理UI层与业务逻辑层的交互
 */
class LearningViewModel : ViewModel() {
    
    private val _uiState = MutableStateFlow(LearningUiState())
    val uiState: StateFlow<LearningUiState> = _uiState.asStateFlow()
    
    // MVP简化：直接创建UseCase实例
    private val teachingPlanUseCase = TeachingPlanUseCase()
    private val teachingTaskUseCase = TeachingTaskUseCase()
    private val testingTaskUseCase = TestingTaskUseCase()
    
    /**
     * 1. 用户打开应用，获取今日教学计划
     */
    fun loadTodayTeachingPlan(studentId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                val plan = teachingPlanUseCase.getTodayTeachingPlan(studentId).getOrThrow()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    currentPlan = plan,
                    currentPhase = LearningPhase.PLAN_LOADED
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "加载教学计划失败"
                )
            }
        }
    }
    
    /**
     * 2. 用户点击开始学习，推送任务给教学Agent
     * 教学阶段：讲解知识点 → 学生答题验证理解
     */
    fun startLearning(studentId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                val plan = _uiState.value.currentPlan ?: return@launch
                
                // 开始第一个教学任务
                val firstKnowledgePoint = plan.newKnowledgePoints.firstOrNull() 
                    ?: plan.reviewKnowledgePoints.firstOrNull()
                
                if (firstKnowledgePoint != null) {
                    val task = teachingTaskUseCase.createTeachingTask(
                        studentId = studentId,
                        knowledgePointId = firstKnowledgePoint,
                        taskType = if (plan.newKnowledgePoints.contains(firstKnowledgePoint)) 
                            TaskType.TEACHING else TaskType.REVIEW
                    ).getOrThrow()
                    
                    val startedTask = teachingTaskUseCase.startTeachingTask(task.taskId).getOrThrow()
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        currentTask = startedTask,
                        currentPhase = LearningPhase.TEACHING
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "开始学习失败"
                )
            }
        }
    }
    
    /**
     * 处理学生回答
     * 教学阶段：验证学生对知识点的理解
     */
    fun handleStudentAnswer(answer: String) {
        viewModelScope.launch {
            val currentTask = _uiState.value.currentTask ?: return@launch
            
            try {
                val result = teachingTaskUseCase.handleStudentAnswer(
                    currentTask.taskId,
                    answer
                ).getOrThrow()
                
                if (result.shouldUpdateProgress) {
                    // 完成任务，更新进度（MVP简化：暂时不更新数据库）
                    // teachingTaskUseCase.completeTeachingTask(...)
                    
                    // 检查是否还有更多教学任务
                    val plan = _uiState.value.currentPlan ?: return@launch
                    val allPoints = plan.newKnowledgePoints + plan.reviewKnowledgePoints
                    val completedPoints = mutableSetOf<String>()
                    
                    // 添加当前完成的知识点
                    completedPoints.add(currentTask.knowledgePointId)
                    
                    val remainingPoints = allPoints.filter { !completedPoints.contains(it) }
                    
                    if (remainingPoints.isNotEmpty()) {
                        // 继续下一个教学任务
                        startLearning(currentTask.studentId)
                    } else {
                        // 所有教学任务完成，进入检验阶段
                        startTesting(currentTask.studentId, allPoints)
                    }
                } else {
                    // 更新当前任务状态
                    _uiState.value = _uiState.value.copy(
                        currentTask = currentTask.copy(
                            noResponseCount = currentTask.noResponseCount + 1
                        ),
                        feedback = result.feedback
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "处理答案失败"
                )
            }
        }
    }
    
    /**
     * 3. 开始检验阶段
     * 检验阶段：教学完成后，出题检验学生掌握情况
     */
    private fun startTesting(studentId: String, knowledgePointIds: List<String>) {
        viewModelScope.launch {
            try {
                val testingTask = testingTaskUseCase.createTestingTask(
                    studentId,
                    knowledgePointIds
                ).getOrThrow()
                
                val startedTestingTask = testingTaskUseCase.startTestingTask(testingTask.taskId).getOrThrow()
                
                _uiState.value = _uiState.value.copy(
                    currentTestingTask = startedTestingTask,
                    currentPhase = LearningPhase.TESTING
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "开始检验失败"
                )
            }
        }
    }
    
    /**
     * 提交检验答案
     * 检验阶段：学生答题，AI评判，更新掌握状态
     */
    fun submitTestingAnswer(answer: String, imageAnswer: String? = null) {
        viewModelScope.launch {
            val currentTestingTask = _uiState.value.currentTestingTask ?: return@launch
            
            try {
                val currentQuestion = currentTestingTask.questions[currentTestingTask.currentQuestionIndex]
                val result = testingTaskUseCase.submitStudentAnswer(
                    currentTestingTask.taskId,
                    currentQuestion.questionId,
                    currentTestingTask.studentId,
                    answer,
                    imageAnswer
                ).getOrThrow()
                
                _uiState.value = _uiState.value.copy(
                    currentTestingResult = result,
                    feedback = result.feedback
                )
                
                // 检查是否完成所有题目
                if (currentTestingTask.currentQuestionIndex >= currentTestingTask.questions.size - 1) {
                    // 完成检验任务（MVP简化：暂时不更新数据库）
                    // testingTaskUseCase.completeTestingTask(...)
                    
                    _uiState.value = _uiState.value.copy(
                        currentPhase = LearningPhase.COMPLETED,
                        achievement = "恭喜完成今日学习！"
                    )
                } else {
                    // 下一题
                    _uiState.value = _uiState.value.copy(
                        currentTestingTask = currentTestingTask.copy(
                            currentQuestionIndex = currentTestingTask.currentQuestionIndex + 1
                        )
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "提交答案失败"
                )
            }
        }
    }
    
    /**
     * 清除错误
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

/**
 * 学习UI状态
 */
data class LearningUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentPhase: LearningPhase = LearningPhase.INITIAL,
    val currentPlan: TeachingPlan? = null,
    val currentTask: TeachingTask? = null,
    val currentTestingTask: TestingTask? = null,
    val currentTestingResult: TestingResult? = null,
    val feedback: String? = null,
    val achievement: String? = null
)

/**
 * 学习阶段枚举
 */
enum class LearningPhase {
    INITIAL,        // 初始状态
    PLAN_LOADED,    // 教学计划已加载
    TEACHING,       // 教学阶段
    TESTING,        // 检验阶段
    COMPLETED       // 完成
}
