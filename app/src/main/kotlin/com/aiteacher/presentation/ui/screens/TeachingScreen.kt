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
                    text = task.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Text(
                    text = task.description,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                Text(
                    text = task.content,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                // 示例简化：不显示问题列表，按需扩展
                
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

