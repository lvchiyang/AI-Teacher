package com.aiteacher.presentation.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aiteacher.ai.service.LLMModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LlmTestScreen(
    onBack: () -> Unit
) {
    var userInput by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val messages = remember { mutableStateListOf<Message>() }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    
    val llmModel = remember { LLMModel() }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("LLM API 测试") },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("< 返回", color = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 消息列表
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(messages) { message ->
                    MessageItem(message = message)
                }
            }
            
            // 输入框
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = userInput,
                    onValueChange = { userInput = it },
                    label = { Text("输入消息") },
                    modifier = Modifier.weight(1f),
                    enabled = !isLoading
                )
                Button(
                    onClick = {
                        if (userInput.isNotBlank() && !isLoading) {
                            val userMessage = userInput
                            userInput = ""
                            
                            // 添加用户消息
                            messages.add(Message(
                                text = userMessage,
                                isUser = true,
                                timestamp = System.currentTimeMillis()
                            ))
                            
                            isLoading = true
                            
                            scope.launch {
                                try {
                                    // 添加助手消息占位
                                    val aiMessageIndex = messages.size
                                    messages.add(Message(
                                        text = "思考中...",
                                        isUser = false,
                                        timestamp = System.currentTimeMillis()
                                    ))
                                    
                                    // 调用 LLM
                                    val result = llmModel.generateText(userMessage)
                                    
                                    // 更新助手消息
                                    result?.let { output ->
                                        messages[aiMessageIndex] = Message(
                                            text = output.content,
                                            isUser = false,
                                            timestamp = System.currentTimeMillis()
                                        )
                                    }
                                    
                                    // 滚动到底部
                                    scope.launch {
                                        listState.animateScrollToItem(messages.size - 1)
                                    }
                                } catch (e: Exception) {
                                    messages.add(Message(
                                        text = "错误: ${e.message}",
                                        isUser = false,
                                        timestamp = System.currentTimeMillis()
                                    ))
                                } finally {
                                    isLoading = false
                                }
                            }
                        }
                    },
                    enabled = !isLoading && userInput.isNotBlank()
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("发送")
                    }
                }
            }
        }
    }
}

@Composable
fun MessageItem(message: Message) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
            modifier = Modifier
                .widthIn(max = 300.dp)
                .padding(horizontal = 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (message.isUser)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Text(
                text = message.text,
                modifier = Modifier.padding(12.dp),
                color = if (message.isUser)
                    MaterialTheme.colorScheme.onPrimary
                else
                    MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

data class Message(
    val text: String,
    val isUser: Boolean,
    val timestamp: Long
)

