package com.aiteacher.presentation.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aiteacher.presentation.viewmodel.LearningViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentDashboardScreen(
    studentId: String,
    studentName: String,
    onNavigateBack: () -> Unit = {},
    viewModel: LearningViewModel = remember { LearningViewModel() }
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // 加载学生进度概览
    LaunchedEffect(studentId) {
        // 这里可以调用获取学生进度概览的方法
    }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // 顶部标题栏
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "家长监督 - $studentName",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                
                TextButton(onClick = onNavigateBack) {
                    Text("返回")
                }
            }
        }
        
        item {
            // 学习概览卡片
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "学习概览",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "5",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "已完成题目",
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                        
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "85%",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Text(
                                text = "正确率",
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                        
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "30",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                            Text(
                                text = "学习时长(分钟)",
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
        
        item {
            // 当前学习状态卡片
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "当前学习状态",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    when (uiState.currentPhase) {
                        com.aiteacher.presentation.viewmodel.LearningPhase.INITIAL -> {
                            Text(
                                text = "状态：未开始学习",
                                fontSize = 14.sp,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                        com.aiteacher.presentation.viewmodel.LearningPhase.PLAN_LOADED -> {
                            Text(
                                text = "状态：教学计划已制定",
                                fontSize = 14.sp,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            uiState.currentPlan?.let { plan ->
                                Text(
                                    text = "当前章节：${plan.currentChapter}",
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                                Text(
                                    text = "预计时长：${plan.estimatedDuration}分钟",
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                            }
                        }
                        com.aiteacher.presentation.viewmodel.LearningPhase.TEACHING -> {
                            Text(
                                text = "状态：正在教学",
                                fontSize = 14.sp,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            uiState.currentTask?.let { task ->
                                Text(
                                    text = "当前知识点：${task.knowledgePointId}",
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                                Text(
                                    text = "任务类型：${if (task.taskType == com.aiteacher.domain.model.TaskType.TEACHING) "教学" else "复习"}",
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                            }
                        }
                        com.aiteacher.presentation.viewmodel.LearningPhase.TESTING -> {
                            Text(
                                text = "状态：正在检验",
                                fontSize = 14.sp,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            uiState.currentTestingTask?.let { task ->
                                Text(
                                    text = "题目进度：${task.currentQuestionIndex + 1}/${task.questions.size}",
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                            }
                        }
                        com.aiteacher.presentation.viewmodel.LearningPhase.COMPLETED -> {
                            Text(
                                text = "状态：学习完成",
                                fontSize = 14.sp,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            uiState.achievement?.let { achievement ->
                                Text(
                                    text = "成就：$achievement",
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
        
        item {
            // 学习报告卡片
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "今日学习报告",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    Text(
                        text = "• 完成了5道数学题",
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    
                    Text(
                        text = "• 正确率85%，表现良好",
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    
                    Text(
                        text = "• 学习时长30分钟，注意力集中",
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    
                    Text(
                        text = "• AI助手互动3次，学习积极性高",
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    Button(
                        onClick = { /* 查看详细报告 */ },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("查看详细报告")
                    }
                }
            }
        }
        
        item {
            // 学习建议卡片
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "学习建议",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    Text(
                        text = "根据孩子的学习表现，建议：",
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    Text(
                        text = "1. 继续保持当前的学习节奏",
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    
                    Text(
                        text = "2. 可以适当增加练习题的难度",
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    
                    Text(
                        text = "3. 鼓励孩子多与AI助手互动",
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    Button(
                        onClick = { /* 设置学习计划 */ },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("设置学习计划")
                    }
                }
            }
        }
    }
}