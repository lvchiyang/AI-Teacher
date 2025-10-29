package com.aiteacher

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.aiteacher.presentation.navigation.AITeacherNavigation
import com.aiteacher.presentation.ui.theme.AITeacherTheme
import com.aiteacher.presentation.viewmodel.MainViewModel
import com.aiteacher.service.AgentService
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.flow.collectLatest
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class MainActivity : ComponentActivity(), KoinComponent {
    
    // 使用Koin的inject获取单例的MainViewModel
    private val viewModel: MainViewModel by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 启动Agent服务
        startAgentService()
        
        setContent {
            AITeacherTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(viewModel = viewModel)
                }
            }
        }
    }

    private fun startAgentService() {
        val intent = Intent(this, AgentService::class.java).apply {
            action = AgentService.ACTION_START_AGENTS
        }
        startService(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        // 停止Agent服务
        val intent = Intent(this, AgentService::class.java).apply {
            action = AgentService.ACTION_STOP_AGENTS
        }
        startService(intent)
    }
}

@Composable
fun MainScreen(viewModel: MainViewModel) {
    // 使用本地状态
    var uiState by remember { mutableStateOf<com.aiteacher.presentation.viewmodel.MainUiState>(com.aiteacher.presentation.viewmodel.MainUiState()) }
    var currentStudent by remember { mutableStateOf<com.aiteacher.domain.model.Student?>(null) }
    
    // 收集UI状态
    LaunchedEffect(Unit) {
        viewModel.uiState.collectLatest { 
            uiState = it 
        }
    }
    
    // 收集当前学生
    LaunchedEffect(Unit) {
        viewModel.currentStudent.collectLatest { 
            currentStudent = it 
        }
    }
    
    // 监听初始化状态
    LaunchedEffect(uiState.isInitialized) {
        if (uiState.isInitialized) {
            // 应用初始化完成，可以执行相关逻辑
        }
    }
    
    val navController = rememberNavController()
    AITeacherNavigation(
        navController = navController,
        studentRepository = AITeacherApplication.getInstance().studentRepository
    )
}