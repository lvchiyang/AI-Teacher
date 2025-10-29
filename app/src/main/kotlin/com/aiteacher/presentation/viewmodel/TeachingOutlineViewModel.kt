package com.aiteacher.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aiteacher.ai.agent.TeachingPlanResult
import com.aiteacher.domain.usecase.TeachingOutlineUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch

/**
 * 教学大纲ViewModel
 * 管理教学大纲的UI状态和业务逻辑
 */
class TeachingOutlineViewModel(
    private val useCase: TeachingOutlineUseCase,
    private val mainViewModel: MainViewModel
) : ViewModel() {
    
    // 使用Channel发送UI状态更新
    private val _uiStateChannel = Channel<TeachingOutlineUiState>(Channel.CONFLATED)
    val uiState: Flow<TeachingOutlineUiState> = _uiStateChannel.receiveAsFlow()
    
    init {
        android.util.Log.d("TeachingOutlineViewModel", "TeachingOutlineViewModel 初始化完成")
        // 直接读取MainViewModel.currentStudent的最新值
        val student = mainViewModel.currentStudent.value
        android.util.Log.d("TeachingOutlineViewModel", "MainViewModel.currentStudent.value = $student")
        
        // 初始化时加载大纲
        viewModelScope.launch {
            if (student != null) {
                loadTeachingOutline(student.studentId, student.grade)
            } else {
                android.util.Log.e("TeachingOutlineViewModel", "学生信息为null")
                _uiStateChannel.send(
                    TeachingOutlineUiState(
                        isLoading = false,
                        outline = null,
                        error = "学生信息未找到"
                    )
                )
            }
        }
    }
    
    /**
     * 加载教学大纲
     */
    fun loadTeachingOutline(studentId: String, grade: Int) {
        viewModelScope.launch {
            try {
                // 发送加载状态
                _uiStateChannel.send(TeachingOutlineUiState(isLoading = true))
                
                // 调用UseCase生成大纲
                val result = useCase.generateOutline(studentId, grade)
                
                if (result.isSuccess) {
                    // 发送成功状态
                    _uiStateChannel.send(
                        TeachingOutlineUiState(
                            isLoading = false,
                            outline = result.getOrNull(),
                            error = null
                        )
                    )
                } else {
                    // 发送错误状态
                    _uiStateChannel.send(
                        TeachingOutlineUiState(
                            isLoading = false,
                            outline = null,
                            error = result.exceptionOrNull()?.message ?: "生成教学大纲失败"
                        )
                    )
                }
            } catch (e: Exception) {
                // 发送错误状态
                _uiStateChannel.send(
                    TeachingOutlineUiState(
                        isLoading = false,
                        outline = null,
                        error = e.message ?: "加载教学大纲失败"
                    )
                )
            }
        }
    }
    
    /**
     * 重试生成大纲
     */
    fun retry(studentId: String, grade: Int) {
        loadTeachingOutline(studentId, grade)
    }
}

/**
 * 教学大纲UI状态
 */
data class TeachingOutlineUiState(
    val isLoading: Boolean = false,
    val outline: TeachingPlanResult? = null,
    val error: String? = null
)

