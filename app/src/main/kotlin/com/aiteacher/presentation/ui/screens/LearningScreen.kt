package com.aiteacher.presentation.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aiteacher.data.local.repository.StudentRepository
import com.aiteacher.domain.model.TestingResult
import com.aiteacher.presentation.viewmodel.LearningViewModel
import com.aiteacher.presentation.viewmodel.LearningPhase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LearningScreen(
    studentId: String,
    studentName: String,
    grade: Int,
    onNavigateToParent: () -> Unit = {},
    onBackToHome: () -> Unit = {},
    studentRepository: StudentRepository
) {
    val viewModel = remember { LearningViewModel(studentRepository) }
    val uiState by viewModel.uiState.collectAsState()
    
    // 初始化加载教学计划
    LaunchedEffect(studentId) {
        viewModel.loadTodayTeachingPlan(studentId)
    }
    
    // 错误处理
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            // 显示错误信息
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (uiState.currentPhase) {
            LearningPhase.INITIAL -> {
                if (uiState.isLoading) {
                    LoadingScreen()
                } else if (uiState.error != null) {
                    ErrorScreen(
                        error = uiState.error!!,
                        onRetry = { 
                            viewModel.clearError()
                            viewModel.loadTodayTeachingPlan(studentId) 
                        }
                    )
                } else {
                    LoadingScreen() // 默认显示加载状态
                }
            }
            
            LearningPhase.PLAN_LOADED -> {
                // 创建Domain模型
                val domainPlan = com.aiteacher.domain.model.TeachingPlan(
                    planId = uiState.currentPlan!!.id,
                    studentId = studentId,
                    date = "2024-01-01",
                    grade = grade,
                    currentChapter = uiState.currentPlan!!.currentChapter,
                    reviewKnowledgePoints = emptyList(),
                    newKnowledgePoints = emptyList(),
                    estimatedDuration = uiState.currentPlan!!.estimatedDuration,
                    status = com.aiteacher.domain.model.PlanStatus.PENDING
                )
                TeachingPlanScreen(
                    plan = domainPlan,
                    onStartLearning = { 
                        viewModel.startLearning(domainPlan) 
                    }
                )
            }
            
            LearningPhase.TEACHING -> {
                // 创建Domain模型的TeachingTask
                val domainTask = com.aiteacher.domain.model.TeachingTask(
                    taskId = uiState.currentTask!!.id,
                    studentId = studentId,
                    knowledgePointId = uiState.currentTask!!.knowledgePointId,
                    taskType = com.aiteacher.domain.model.TaskType.TEACHING,
                    content = com.aiteacher.domain.model.TeachingContent(
                        text = uiState.currentTask!!.content,
                        images = emptyList(),
                        audio = null,
                        ppt = null
                    ),
                    questions = emptyList(),
                    status = com.aiteacher.domain.model.TaskStatus.IN_PROGRESS,
                    currentQuestionIndex = 0,
                    noResponseCount = 0
                )
                TeachingScreen(
                    task = domainTask,
                    feedback = uiState.feedback,
                    onAnswerSubmit = { answer -> viewModel.handleStudentAnswer(answer) },
                    onContinueNext = { viewModel.continueToNextTask() }
                )
            }
            
            LearningPhase.TESTING -> {
                // 创建Domain模型的TestingTask
                val domainTestingTask = com.aiteacher.domain.model.TestingTask(
                    taskId = uiState.currentTestingTask!!.id,
                    studentId = studentId,
                    knowledgePointIds = emptyList(),
                    questions = emptyList(),
                    status = com.aiteacher.domain.model.TaskStatus.IN_PROGRESS,
                    currentQuestionIndex = uiState.currentTestingTask!!.currentQuestionIndex,
                    startTime = "2024-01-01T00:00:00",
                    timeLimit = 30
                )
                TestingScreen(
                    testingTask = domainTestingTask,
                    result = null, // 暂时设为null
                    feedback = uiState.feedback,
                    onAnswerSubmit = { answer, imageAnswer -> 
                        viewModel.submitTestingAnswer(answer, "question_id") 
                    }
                )
            }
            
            LearningPhase.PLANNING -> {
                Text(
                    text = "正在制定教学计划...",
                    fontSize = 16.sp,
                    modifier = Modifier.padding(16.dp)
                )
            }
            
            LearningPhase.LEARNING -> {
                Text(
                    text = "正在学习中...",
                    fontSize = 16.sp,
                    modifier = Modifier.padding(16.dp)
                )
            }
            
            LearningPhase.COMPLETED -> {
                CompletedScreen(
                    achievement = uiState.achievement?.title ?: "学习完成！",
                    onRestart = { viewModel.loadTodayTeachingPlan(studentId) },
                    onNavigateToParent = onNavigateToParent,
                    onBackToHome = onBackToHome
                )
            }
        }
    }
}

@Composable
fun LoadingScreen() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator()
        Spacer(modifier = Modifier.height(16.dp))
        Text("加载中...", fontSize = 16.sp)
    }
}

@Composable
fun ErrorScreen(
    error: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "错误：$error",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text("重试")
        }
    }
}

@Composable
fun TeachingPlanScreen(
    plan: com.aiteacher.domain.model.TeachingPlan,
    onStartLearning: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "今日教学计划",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "年级：${plan.grade}年级",
                    fontSize = 16.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Text(
                    text = "当前章节：${plan.currentChapter}",
                    fontSize = 16.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Text(
                    text = "预计时长：${plan.estimatedDuration}分钟",
                    fontSize = 16.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                if (plan.reviewKnowledgePoints.isNotEmpty()) {
                    Text(
                        text = "复习知识点：",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    plan.reviewKnowledgePoints.forEach { point ->
                        Text(
                            text = "• $point",
                            fontSize = 14.sp,
                            modifier = Modifier.padding(start = 16.dp, bottom = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                if (plan.newKnowledgePoints.isNotEmpty()) {
                    Text(
                        text = "新学知识点：",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    plan.newKnowledgePoints.forEach { point ->
                        Text(
                            text = "• $point",
                            fontSize = 14.sp,
                            modifier = Modifier.padding(start = 16.dp, bottom = 2.dp)
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
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
    }
}

@Composable
fun TeachingScreen(
    task: com.aiteacher.domain.model.TeachingTask,
    feedback: String?,
    onAnswerSubmit: (String) -> Unit,
    onContinueNext: () -> Unit = {}
) {
    var answer by remember { mutableStateOf("") }
    
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "教学阶段",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "知识点：${task.knowledgePointId}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Text(
                    text = "任务类型：${if (task.taskType == com.aiteacher.domain.model.TaskType.TEACHING) "教学" else "复习"}",
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                Text(
                    text = task.content.text,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                if (task.questions.isNotEmpty()) {
                    val currentQuestion = task.questions[task.currentQuestionIndex]
                    Text(
                        text = "问题：${currentQuestion.content}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                
                OutlinedTextField(
                    value = answer,
                    onValueChange = { answer = it },
                    label = { Text("请输入答案") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )
                
                Button(
                    onClick = { onAnswerSubmit(answer) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("提交答案")
                }
            }
        }
        
        feedback?.let { feedbackText ->
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = feedbackText,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    // 如果回答正确，显示继续按钮
                    if (feedbackText.contains("回答正确")) {
                        Button(
                            onClick = { 
                                // 清空答案和反馈，准备下一题
                                answer = ""
                                onContinueNext()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("进入测试阶段")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TestingScreen(
    testingTask: com.aiteacher.domain.model.TestingTask,
    result: TestingResult?,
    feedback: String?,
    onAnswerSubmit: (String, String?) -> Unit
) {
    var answer by remember { mutableStateOf("") }
    
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "检验阶段",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        if (testingTask.questions.isNotEmpty()) {
            val currentQuestion = testingTask.questions[testingTask.currentQuestionIndex]
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "题目 ${testingTask.currentQuestionIndex + 1}/${testingTask.questions.size}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    Text(
                        text = "知识点：${currentQuestion.knowledgePointId}",
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    Text(
                        text = "分值：${currentQuestion.points}分",
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    Text(
                        text = "时间限制：${currentQuestion.timeLimit}分钟",
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    Text(
                        text = currentQuestion.content,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    OutlinedTextField(
                        value = answer,
                        onValueChange = { answer = it },
                        label = { Text("请输入答案") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    )
                    
                    Button(
                        onClick = { onAnswerSubmit(answer, null) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("提交答案")
                    }
                }
            }
            
            result?.let { testResult: TestingResult ->
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (testResult.correctCount > 0) 
                            MaterialTheme.colorScheme.primaryContainer 
                        else MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "测试完成！得分：${testResult.totalScore}/${testResult.maxScore}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        Text(
                            text = "正确题数：${testResult.correctCount}/${testResult.totalCount}",
                            fontSize = 14.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        Text(
                            text = "用时：${testResult.timeSpent}秒",
                            fontSize = 14.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                }
            }
            
            feedback?.let { feedbackText ->
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Text(
                        text = feedbackText,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CompletedScreen(
    achievement: String,
    onRestart: () -> Unit,
    onNavigateToParent: () -> Unit = {},
    onBackToHome: () -> Unit = {}
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "🎉",
            fontSize = 64.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        Text(
            text = achievement,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = onBackToHome,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    text = "返回主页",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = onRestart,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                ) {
                    Text(
                        text = "重新开始",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Button(
                    onClick = onNavigateToParent,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                ) {
                    Text(
                        text = "家长监督",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}