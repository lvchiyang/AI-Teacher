package com.aiteacher.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aiteacher.data.local.repository.StudentRepository
import com.aiteacher.domain.model.Student
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 主界面ViewModel
 * 管理应用状态和业务逻辑
 */
class MainViewModel(
    private val studentRepository: StudentRepository
) : ViewModel() {

    // UI状态
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    // 当前学生信息
    private val _currentStudent = MutableStateFlow<Student?>(null)
    val currentStudent: StateFlow<Student?> = _currentStudent.asStateFlow()

    // 应用初始化状态
    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()

    init {
        initializeApp()
    }

    /**
     * 初始化应用
     */
    private fun initializeApp() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                
                // 加载学生信息
                loadCurrentStudent()
                
                _isInitialized.value = true
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isInitialized = true
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    /**
     * 加载当前学生信息
     */
    private suspend fun loadCurrentStudent() {
        try {
            val students = studentRepository.getAllStudents()
            if (students.isNotEmpty()) {
                _currentStudent.value = students.first()
            }
        } catch (e: Exception) {
            // 处理错误
        }
    }

    /**
     * 设置当前学生
     */
    fun setCurrentStudent(student: Student) {
        _currentStudent.value = student
    }

    /**
     * 清除当前学生
     */
    fun clearCurrentStudent() {
        _currentStudent.value = null
    }

    /**
     * 刷新数据
     */
    fun refresh() {
        initializeApp()
    }

    /**
     * 清除错误
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
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
