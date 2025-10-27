package com.aiteacher.presentation.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 教学阶段界面
 * 显示教学内容和问题
 */
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
                    
                    // 显示继续按钮
                    Button(
                        onClick = { 
                            // 清空答案和反馈，准备下一题
                            answer = ""
                            onContinueNext()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("继续下一步")
                    }
                }
            }
        }
    }
}

