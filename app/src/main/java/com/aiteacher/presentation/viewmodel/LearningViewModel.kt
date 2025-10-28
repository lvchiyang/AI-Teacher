package com.aiteacher.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aiteacher.data.local.repository.*
import com.aiteacher.domain.model.*
import com.aiteacher.domain.usecase.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 学习ViewModel
 * 处理UI层与业务逻辑层的交互
 */
class LearningViewModel(
    private val studentRepository: StudentRepository,
    private val teachingPlanRepository: TeachingPlanRepository,
    private val teachingTaskRepository: TeachingTaskRepository,
    private val questionRepository: QuestionRepository,
    private val testingTaskRepository: TestingTaskRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(LearningUiState())
    val uiState: StateFlow<LearningUiState> = _uiState.asStateFlow()
    
    // MVP简化：直接创建UseCase实例
    private val studentUseCase = StudentUseCase(studentRepository, teachingTaskRepository)
    private val teachingPlanUseCase = TeachingPlanUseCase(teachingPlanRepository)
    private val teachingTaskUseCase = TeachingTaskUseCase(teachingTaskRepository)
    private val testingTaskUseCase = TestingTaskUseCase(testingTaskRepository, questionRepository)
    
    /**
     * 1. 用户打开应用，获取所有教学计划
     */
    fun loadTodayTeachingPlan(studentId: String) {
        if (studentId.isBlank()) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                error = "学生ID不能为空"
            )
            return
        }
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                val planResult = teachingPlanUseCase.getTodayTeachingPlan(studentId)
                if (planResult.isSuccess) {
                    val plan = planResult.getOrNull() ?: emptyList()
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        currentPlans = plan,
                        currentPhase = LearningPhase.PLAN_LOADED
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = planResult.exceptionOrNull()?.message ?: "加载教学计划失败"
                    )
                }
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
    fun startLearning(studentId: String, planId: String) {
        if (studentId.isBlank()) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                error = "学生ID不能为空"
            )
            return
        }
        
        if (planId.isBlank()) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                error = "计划ID不能为空"
            )
            return
        }
        
        // 检查currentTasks是否为空
        val currentTasks = _uiState.value.currentTasks
        if (currentTasks.isNullOrEmpty()) {
            // 如果为空，则获取所有教学计划
            loadTodayTeachingPlan(studentId)
            return
        }
        
        // 若不为空，则启动列表中的第一个任务
        val firstTask = currentTasks.firstOrNull()
        if (firstTask == null) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                error = "当前任务列表为空"
            )
            return
        }
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                val taskResult = teachingTaskUseCase.startTeachingTask(firstTask.taskId)
                if (taskResult.isSuccess) {
                    val startedTask = taskResult.getOrNull()
                    if (startedTask != null) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            currentTasks = listOf(startedTask),
                            currentPhase = LearningPhase.TEACHING
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "启动教学任务失败"
                        )
                    }
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = taskResult.exceptionOrNull()?.message ?: "启动教学任务失败"
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
     * 继续下一个教学任务
     */
    fun continueToNextTask() {
        val currentTasks = _uiState.value.currentTasks
        
        // 若currentTasks为空，则调用加载教学计划功能
        if (currentTasks.isNullOrEmpty()) {
            // 这里需要学生ID来加载教学计划，但此函数没有参数
            // 可以考虑从现有的UI状态中获取学生ID，如果存在的话
            _uiState.value = _uiState.value.copy(error = "无法加载教学计划：缺少学生ID")
            return
        }
        
        // 若不为空，则将第一个任务更新为已完成，移出currentTasks列表
        val firstTask = currentTasks.firstOrNull()
        if (firstTask == null) {
            _uiState.value = _uiState.value.copy(error = "当前任务列表为空")
            return
        }
        
        // 更新第一个任务为已完成状态
        viewModelScope.launch {
            try {
                val result = teachingTaskUseCase.completeTeachingTask(firstTask.taskId)
                if (result.isSuccess) {
                    // 从currentTasks列表中移除已完成的任务
                    val remainingTasks = currentTasks.drop(1)
                    
                    // 更新UI状态
                    _uiState.value = _uiState.value.copy(
                        currentTasks = remainingTasks.ifEmpty { null },
                        feedback = "任务已完成"
                    )
                    
                    // 如果还有剩余任务，可以考虑自动开始下一个任务
                    // 或者保持当前状态，等待用户进一步操作
                    
                    // 如果没有剩余任务，可以加载教学计划
                    if (remainingTasks.isEmpty()) {
                        _uiState.value = _uiState.value.copy(
                            error = "所有任务已完成，请加载新的教学计划",
                            currentPhase = LearningPhase.PLAN_LOADED
                        )
                    }
                } else {
                    _uiState.value = _uiState.value.copy(
                        error = result.exceptionOrNull()?.message ?: "完成任务失败"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "处理任务完成时发生错误"
                )
            }
        }
    }
    
    /**
     * 处理学生回答
     * 教学阶段：验证学生对知识点的理解
     */
    fun handleStudentAnswer(answer: String) {
        if (answer.isBlank()) {
            _uiState.value = _uiState.value.copy(
                error = "答案不能为空"
            )
            return
        }
        
        viewModelScope.launch {
            val currentTask = _uiState.value.currentTasks?.firstOrNull() ?: run {
                _uiState.value = _uiState.value.copy(
                    error = "当前任务为空"
                )
                return@launch
            }
            
            try {
                val result = teachingTaskUseCase.handleStudentAnswer(
                    currentTask.taskId,
                    answer
                )
                
                if (result.isSuccess) {
                    val answerResult = result.getOrNull()
                    if (answerResult != null) {
                        // 先更新反馈信息
                        _uiState.value = _uiState.value.copy(
                            feedback = answerResult.feedback
                        )
                        
                        if (answerResult.shouldUpdateProgress) {
                            // 更新学生学习进度：完成教学任务
                            val progressResult = studentUseCase.completeTeachingTask(
                                currentTask
                            )
                            
                            if (progressResult.isFailure) {
                                _uiState.value = _uiState.value.copy(
                                    error = progressResult.exceptionOrNull()?.message ?: "更新学习进度失败"
                                )
                            }
                        } else {
                            // 更新当前任务状态
                            _uiState.value = _uiState.value.copy(
                                currentTasks = listOf(currentTask.copy(
                                    noResponseCount = currentTask.noResponseCount + 1
                                ))
                            )
                        }
                    } else {
                        _uiState.value = _uiState.value.copy(
                            error = "处理答案结果为空"
                        )
                    }
                } else {
                    _uiState.value = _uiState.value.copy(
                        error = result.exceptionOrNull()?.message ?: "处理答案失败"
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
        if (studentId.isBlank()) {
            _uiState.value = _uiState.value.copy(
                error = "学生ID不能为空"
            )
            return
        }
        
        if (knowledgePointIds.isEmpty()) {
            _uiState.value = _uiState.value.copy(
                error = "知识点列表不能为空"
            )
            return
        }
        
        viewModelScope.launch {
            try {
                // 根据知识点ID获取相关题目
                val questions = mutableListOf<Question>()
                knowledgePointIds.forEach { knowledgeId ->
                    val questionResult = questionRepository.getQuestionsByKnowledgeId(knowledgeId)
                    if (questionResult.isSuccess) {
                        questions.addAll(questionResult.getOrNull() ?: emptyList())
                    }
                }
                
                if (questions.isEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        error = "未找到相关测试题目"
                    )
                    return@launch
                }
                
                // 取前5题作为测试题
                val selectedQuestions = questions.take(5)
                val questionIds = selectedQuestions.map { it.questionId }
                
                val testingTaskResult = testingTaskUseCase.createTestingTask(
                    studentId = studentId,
                    title = "知识点掌握测试",
                    description = "检验对知识点的掌握情况",
                    questionIds = questionIds,
                    passingScore = 60 // 60分及格
                )
                
                if (testingTaskResult.isSuccess) {
                    val testingTask = testingTaskResult.getOrNull()
                    if (testingTask != null) {
                        val startedTestingTaskResult = testingTaskUseCase.startTestingTask(testingTask.taskId)
                        if (startedTestingTaskResult.isSuccess) {
                            val startedTestingTask = startedTestingTaskResult.getOrNull()
                            if (startedTestingTask != null) {
                                _uiState.value = _uiState.value.copy(
                                    currentTest = startedTestingTask,
                                    currentPhase = LearningPhase.TESTING
                                )
                            } else {
                                _uiState.value = _uiState.value.copy(
                                    error = "启动测试任务失败"
                                )
                            }
                        } else {
                            _uiState.value = _uiState.value.copy(
                                error = startedTestingTaskResult.exceptionOrNull()?.message ?: "启动测试任务失败"
                            )
                        }
                    } else {
                        _uiState.value = _uiState.value.copy(
                            error = "创建测试任务失败"
                        )
                    }
                } else {
                    _uiState.value = _uiState.value.copy(
                        error = testingTaskResult.exceptionOrNull()?.message ?: "创建测试任务失败"
                    )
                }
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
    fun submitTestAnswer(questionId: String, answer: String) {
        if (answer.isBlank()) {
            _uiState.value = _uiState.value.copy(
                error = "答案不能为空"
            )
            return
        }
        
        viewModelScope.launch {
            val currentTest = _uiState.value.currentTest ?: run {
                _uiState.value = _uiState.value.copy(
                    error = "当前测试任务为空"
                )
                return@launch
            }
            
            try {
                // 构造答案映射
                val answers = mapOf(questionId to answer)
                
                val result = testingTaskUseCase.submitTestAnswers(
                    currentTest.taskId,
                    answers
                )
                
                if (result.isSuccess) {
                    val testResult = result.getOrNull()
                    if (testResult != null) {
                        _uiState.value = _uiState.value.copy(
                            feedback = testResult.feedback
                        )
                        
                        // 更新学生学习进度：完成测试任务
                        val progressResult = studentUseCase.completeTestingTask(
                            currentTest.studentId,
                            testResult.passed
                        )
                        
                        if (progressResult.isFailure) {
                            _uiState.value = _uiState.value.copy(
                                error = progressResult.exceptionOrNull()?.message ?: "更新学习进度失败"
                            )
                            return@launch
                        }
                        
                        // 完成检验任务
                        _uiState.value = _uiState.value.copy(
                            currentPhase = LearningPhase.COMPLETED,
                            achievement = if (testResult.passed) "恭喜你通过了测试！" else "很遗憾，你没有通过测试，请继续努力！"
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            error = "提交答案结果为空"
                        )
                    }
                } else {
                    _uiState.value = _uiState.value.copy(
                        error = result.exceptionOrNull()?.message ?: "提交答案失败"
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
    val currentPlans: List<TeachingPlan>? = null,
    val currentTasks: List<TeachingTask>? = null,
    val currentTest: TestingTask? = null,
    val feedback: String? = null,
    val achievement: String? = null,
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