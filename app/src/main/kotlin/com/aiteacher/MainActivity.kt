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
import androidx.activity.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {
    
    private val viewModel: MainViewModel by viewModel()

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
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentStudent by viewModel.currentStudent.collectAsStateWithLifecycle()
    
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