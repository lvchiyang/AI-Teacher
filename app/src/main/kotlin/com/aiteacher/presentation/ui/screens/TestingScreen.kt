package com.aiteacher.presentation.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aiteacher.domain.model.TestingResult

/**
 * 检验阶段界面
 * 显示测试题目和结果
 */
@Composable
fun TestingScreen(
    testingTask: com.aiteacher.domain.model.TestingTask,
    result: TestingResult?,
    feedback: String?,
    onAnswerSubmit: (String, String?) -> Unit,
    onBackHome: () -> Unit = {}
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
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = feedbackText,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        
                        // 如果回答正确，显示返回主页按钮
                        if (feedbackText.contains("回答正确")) {
                            Button(
                                onClick = { 
                                    answer = ""
                                    onBackHome()
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("返回主页")
                            }
                        }
                    }
                }
            }
        }
    }
}

