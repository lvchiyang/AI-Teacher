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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToMath: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onLogout: () -> Unit,
    onNavigateToLlmTest: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Teacher") },
                actions = {
                    TextButton(onClick = onNavigateToProfile) {
                        Text("个人中心")
                    }
                    TextButton(onClick = onLogout) {
                        Text("退出登录")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "欢迎使用AI教师",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "选择您要学习的科目",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            item {
                SubjectCard(
                    title = "数学学习",
                    description = "初中数学知识点学习与练习",
                    onClick = onNavigateToMath
                )
            }
            
            item {
                SubjectCard(
                    title = "语文学习",
                    description = "初中语文阅读与写作",
                    onClick = { /* TODO: 实现语文学习 */ }
                )
            }
            
            item {
                SubjectCard(
                    title = "英语学习",
                    description = "初中英语词汇与语法",
                    onClick = { /* TODO: 实现英语学习 */ }
                )
            }
            
            item {
                SubjectCard(
                    title = "物理学习",
                    description = "初中物理实验与理论",
                    onClick = { /* TODO: 实现物理学习 */ }
                )
            }
            
            item {
                SubjectCard(
                    title = "化学学习",
                    description = "初中化学基础与实验",
                    onClick = { /* TODO: 实现化学学习 */ }
                )
            }
            
            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            }
            
            item {
                SubjectCard(
                    title = "LLM API 测试",
                    description = "测试大语言模型 API 调用",
                    onClick = onNavigateToLlmTest
                )
            }
        }
    }
}

@Composable
fun SubjectCard(
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = description,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
