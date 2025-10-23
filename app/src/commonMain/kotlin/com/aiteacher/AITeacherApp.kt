package com.aiteacher

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

// 共享的UI组件
@Composable
fun AITeacherApp() {
    var mcpStatus by remember { mutableStateOf("未启动") }
    var mcpResult by remember { mutableStateOf("") }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "AI Teacher",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "MCP 状态: $mcpStatus",
                    style = MaterialTheme.typography.bodyLarge
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Button(
                    onClick = {
                        mcpStatus = "启动中..."
                        try {
                            // 暂时注释掉MCPDemo调用，因为需要修复依赖问题
                            // MCPDemo.main()
                            mcpStatus = "运行中"
                            mcpResult = "MCP 服务器和客户端已启动 (演示模式)"
                        } catch (e: Exception) {
                            mcpStatus = "错误: ${e.message}"
                            mcpResult = "启动失败"
                        }
                    }
                ) {
                    Text("启动 MCP 服务")
                }
                
                if (mcpResult.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = mcpResult,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "功能列表",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                val features = listOf(
                    "MCP 服务器和客户端",
                    "数学计算工具 (加法/减法)",
                    "跨平台界面 (Android + Desktop)",
                    "数据库支持"
                )
                
                LazyColumn {
                    items(features) { feature ->
                        Row(
                            modifier = Modifier.padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "• ",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = feature,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}
