package com.aiteacher.presentation.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.take
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aiteacher.ai.agent.TeachingPlanResult
import com.aiteacher.presentation.viewmodel.TeachingOutlineViewModel
import com.aiteacher.presentation.viewmodel.TeachingOutlineUiState
import org.koin.androidx.compose.koinViewModel
import com.aiteacher.presentation.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeachingOutlineScreen(
    onNavigateToLearning: (String, String, Int) -> Unit,
    onBackToHome: () -> Unit
) {
    // 获取TeachingOutlineViewModel（通过Koin注入）
    val viewModel: TeachingOutlineViewModel = koinViewModel()
    
    // 获取当前学生信息（用于显示和导航）
    val mainViewModel: MainViewModel = org.koin.compose.getKoin().get<MainViewModel>()
    var currentStudent by remember { mutableStateOf<com.aiteacher.domain.model.Student?>(null) }
    
    LaunchedEffect(Unit) {
        mainViewModel.currentStudent.take(1).collect { student ->
            currentStudent = student
        }
    }
    
    // ① 本地状态：可预览、可测试
    var uiState by remember { mutableStateOf<TeachingOutlineUiState>(TeachingOutlineUiState()) }
    
    // ② 生命周期安全收集
    LaunchedEffect(Unit) {
        viewModel.uiState.collectLatest { 
            uiState = it 
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "教学大纲",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        // ④ 纯UI：只读本地uiState
        when {
            uiState.isLoading -> {
                LoadingOutlineScreen()
            }
            uiState.error != null -> {
                ErrorOutlineScreen(
                    error = uiState.error!!
                )
            }
            uiState.outline != null -> {
                currentStudent?.let { student ->
                    TeachingPlanContent(
                        plan = uiState.outline!!,
                        studentName = student.studentName,
                        grade = student.grade,
                        onStartLearning = {
                            onNavigateToLearning(student.studentId, student.studentName, student.grade)
                        },
                        onBackToHome = onBackToHome
                    )
                }
            }
        }
    }
}

@Composable
fun LoadingOutlineScreen() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "正在生成本节课教学大纲...",
            fontSize = 16.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "AI正在分析您的学习进度，制定个性化教学计划",
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ErrorOutlineScreen(
    error: String
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "⚠️",
            fontSize = 48.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        Text(
            text = "生成教学大纲失败",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Text(
            text = error,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(bottom = 24.dp)
        )
    }
}

@Composable
fun TeachingPlanContent(
    plan: TeachingPlanResult,
    studentName: String,
    grade: Int,
    onStartLearning: () -> Unit,
    onBackToHome: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // 学生信息卡片
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "学生信息",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = "姓名：$studentName",
                        fontSize = 16.sp,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(
                        text = "年级：${grade}年级",
                        fontSize = 16.sp,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(
                        text = "预计学习时长：${plan.estimatedDuration}分钟",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
        
        item {
            // 教学计划描述
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "教学计划",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = plan.planDescription,
                        fontSize = 16.sp,
                        lineHeight = 24.sp
                    )
                }
            }
        }
        
        if (plan.reviewKnowledgePoints.isNotEmpty()) {
            item {
                // 复习知识点
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "📚 复习知识点",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        plan.reviewKnowledgePoints.forEach { point ->
                            Text(
                                text = "• $point",
                                fontSize = 16.sp,
                                modifier = Modifier.padding(start = 16.dp, bottom = 4.dp)
                            )
                        }
                    }
                }
            }
        }
        
        if (plan.newKnowledgePoints.isNotEmpty()) {
            item {
                // 新学知识点
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "🎯 新学知识点",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        plan.newKnowledgePoints.forEach { point ->
                            Text(
                                text = "• $point",
                                fontSize = 16.sp,
                                modifier = Modifier.padding(start = 16.dp, bottom = 4.dp)
                            )
                        }
                    }
                }
            }
        }
        
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        item {
            // 操作按钮
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onStartLearning,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text(
                        text = "开始学习",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                OutlinedButton(
                    onClick = onBackToHome,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(
                        text = "返回主页",
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}
