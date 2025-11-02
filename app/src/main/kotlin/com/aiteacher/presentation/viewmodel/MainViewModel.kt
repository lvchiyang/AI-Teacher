package com.aiteacher.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aiteacher.ai.agent.HomeAgent
import com.aiteacher.ai.tool.NavigationTool
import com.aiteacher.data.local.repository.StudentRepository
import com.aiteacher.domain.model.Student
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.util.Date

/**
 * 主界面ViewModel
 * 管理应用状态和业务逻辑
 */
class MainViewModel(
    private val studentRepository: StudentRepository
) : ViewModel() {

    // UI状态
    private val _uiStateChannel = Channel<MainUiState>(Channel.CONFLATED)
    val uiState: Flow<MainUiState> = _uiStateChannel.receiveAsFlow()

    // 当前学生信息 - 使用StateFlow便于直接读取最新值
    private val _currentStudent = kotlinx.coroutines.flow.MutableStateFlow<Student?>(null)
    val currentStudent: kotlinx.coroutines.flow.StateFlow<Student?> = _currentStudent.asStateFlow()
    
    // ========== 对话相关状态（整合自 HomeViewModel）==========
    // 对话消息列表
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()
    
    // 是否正在处理中
    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()
    
    // HomeAgent 实例（延迟初始化，需要 NavigationTool）
    private var homeAgent: HomeAgent? = null
    
    // 导航回调函数（由 UI 层设置）
    private var navigateTo: ((String) -> Unit)? = null

    init {
        initializeApp()
        initializeChatMessages()
    }

    /**
     * 初始化应用
     */
    private fun initializeApp() {
        viewModelScope.launch {
            try {
                _uiStateChannel.send(MainUiState(isLoading = true))
                
                // 加载学生信息（如果有的话）
                val student = loadCurrentStudent()
                _currentStudent.value = student
                
                _uiStateChannel.send(MainUiState(
                    isLoading = false,
                    isInitialized = true
                ))
            } catch (e: Exception) {
                _uiStateChannel.send(MainUiState(
                    isLoading = false,
                    error = e.message
                ))
            }
        }
    }

    /**
     * 加载当前学生信息
     */
    private suspend fun loadCurrentStudent(): Student? {
        val students = studentRepository.getAllStudents()
        return students.firstOrNull()
    }

    /**
     * 设置当前学生（从登录信息加载）
     */
    fun setCurrentStudentFromLogin(studentId: String, studentName: String, grade: Int) {
        android.util.Log.d("MainViewModel", "setCurrentStudentFromLogin: $studentId, $studentName, $grade")
        viewModelScope.launch {
            try {
                val student = studentRepository.getStudentById(studentId)
                if (student != null) {
                    android.util.Log.d("MainViewModel", "成功从数据库获取学生: ${student.studentName}")
                    _currentStudent.value = student
                } else {
                    android.util.Log.e("MainViewModel", "数据库中没有找到学生: $studentId")
                }
            } catch (e: Exception) {
                // 如果找不到学生，创建一个默认的
                val defaultStudent = Student(
                    studentId = studentId,
                    studentName = studentName,
                    grade = grade
                )
                _currentStudent.value = defaultStudent
            }
        }
    }

    /**
     * 刷新数据
     */
    fun refresh() {
        initializeApp()
    }
    
    // ========== 对话相关方法（整合自 HomeViewModel）==========
    
    /**
     * 初始化聊天消息（添加欢迎消息）
     */
    private fun initializeChatMessages() {
        _messages.value = listOf(
            ChatMessage(
                role = "assistant",
                content = "你好！我是AI教师助手，可以帮助你：\n" +
                        "• 学习各学科知识（数学、语文、英语、物理、化学）\n" +
                        "• 做练习题和测试\n" +
                        "• 查看学习统计和进度\n" +
                        "• 管理个人信息\n\n" +
                        "请告诉我你想做什么？",
                timestamp = System.currentTimeMillis()
            )
        )
    }
    
    /**
     * 初始化 HomeAgent（需要在 UI 层设置导航回调后调用）
     * 使用配置文件方式加载工具
     */
    fun initializeAgent(navigateCallback: (String) -> Unit) {
        navigateTo = navigateCallback
        
        // 工具工厂函数：用于创建需要依赖的工具（如 NavigationTool）
        val toolFactory: (String) -> com.aiteacher.ai.tool.BaseTool? = { toolName ->
            when (toolName) {
                "navigate_to_screen" -> NavigationTool(
                    getCurrentStudent = { _currentStudent.value },
                    navigateTo = navigateCallback
                )
                else -> null
            }
        }
        
        // 配置文件路径（相对于项目根目录或使用绝对路径）
        // 注意：在实际运行时，需要使用正确的文件路径
        // 这里使用相对路径，实际项目中可能需要从assets或files目录读取
        val configPath = "app/src/main/kotlin/com/aiteacher/ai/agent/configs/home_tools.json"
        
        // 从配置文件创建 HomeAgent（支持依赖注入的工具）
        homeAgent = HomeAgent(
            toolsConfigPath = configPath,
            toolFactory = toolFactory
        )
        
        // 如果学生信息还未加载，在 viewModelScope 中等待
        val currentStudent = _currentStudent.value
        if (currentStudent == null) {
            viewModelScope.launch {
                // 等待学生信息加载完成（最多等待一段时间）
                var retryCount = 0
                while (retryCount < 10 && _currentStudent.value == null) {
                    kotlinx.coroutines.delay(100)
                    retryCount++
                }
                // 即使等待超时，Agent 也已经创建了，可以在运行时再检查学生信息
            }
        }
    }
    
    /**
     * 发送用户消息
     */
    fun sendMessage(userInput: String) {
        if (userInput.isBlank() || _isProcessing.value) return
        
        // 添加用户消息
        val userMessage = ChatMessage(
            role = "user",
            content = userInput,
            timestamp = System.currentTimeMillis()
        )
        _messages.value = _messages.value + userMessage
        _isProcessing.value = true
        
        // 确保 Agent 已初始化
        if (homeAgent == null) {
            // 如果导航回调已设置，初始化 Agent
            navigateTo?.let { initializeAgent(it) }
        }
        
        viewModelScope.launch {
            try {
                // 调用 HomeAgent
                val agent = homeAgent
                if (agent != null) {
                    val result = agent.runReAct(userInput)
                    
                    result.onSuccess { response ->
                        // 添加助手回复
                        val assistantMessage = ChatMessage(
                            role = "assistant",
                            content = response,
                            timestamp = System.currentTimeMillis()
                        )
                        _messages.value = _messages.value + assistantMessage
                    }.onFailure { error ->
                        // 添加错误消息
                        val errorMessage = ChatMessage(
                            role = "assistant",
                            content = "抱歉，处理您的请求时出现了错误：${error.message}",
                            timestamp = System.currentTimeMillis()
                        )
                        _messages.value = _messages.value + errorMessage
                    }
                } else {
                    // Agent 未初始化
                    val errorMessage = ChatMessage(
                        role = "assistant",
                        content = "系统正在初始化，请稍候...",
                        timestamp = System.currentTimeMillis()
                    )
                    _messages.value = _messages.value + errorMessage
                }
            } catch (e: Exception) {
                val errorMessage = ChatMessage(
                    role = "assistant",
                    content = "处理消息时出错：${e.message}",
                    timestamp = System.currentTimeMillis()
                )
                _messages.value = _messages.value + errorMessage
            } finally {
                _isProcessing.value = false
            }
        }
    }
    
    /**
     * 清空对话历史
     */
    fun clearMessages() {
        _messages.value = emptyList()
        homeAgent?.let {
            // 重新初始化 Agent 以清空记忆
            navigateTo?.let { callback ->
                // 使用配置文件方式重新创建 Agent
                val toolFactory: (String) -> com.aiteacher.ai.tool.BaseTool? = { toolName ->
                    when (toolName) {
                        "navigate_to_screen" -> NavigationTool(
                            getCurrentStudent = { _currentStudent.value },
                            navigateTo = callback
                        )
                        else -> null
                    }
                }
                val configPath = "app/src/main/kotlin/com/aiteacher/ai/agent/configs/home_tools.json"
                homeAgent = HomeAgent(
                    toolsConfigPath = configPath,
                    toolFactory = toolFactory
                )
            }
        }
        
        // 重新添加欢迎消息
        initializeChatMessages()
    }
}

/**
 * 主界面UI状态
 */
data class MainUiState(
    val isLoading: Boolean = false,
    val isInitialized: Boolean = false,
    val error: String? = null
)

/**
 * 聊天消息数据类
 */
data class ChatMessage(
    val role: String, // "user" 或 "assistant"
    val content: String,
    val timestamp: Long
)
