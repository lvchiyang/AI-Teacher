package com.aiteacher.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

    init {
        initializeApp()
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
                    grade = grade,
                    currentChapter = "第一章 有理数",
                    learningProgress = com.aiteacher.domain.model.LearningProgress(
                        notTaught = emptyList(),
                        taughtToReview = emptyList(),
                        notMastered = emptyList(),
                        basicMastery = emptyList(),
                        fullMastery = emptyList(),
                        lastUpdateTime = Date().toString()
                    )
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
}

/**
 * 主界面UI状态
 */
data class MainUiState(
    val isLoading: Boolean = false,
    val isInitialized: Boolean = false,
    val error: String? = null
)
