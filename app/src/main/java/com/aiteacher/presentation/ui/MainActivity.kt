package com.aiteacher.presentation.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.aiteacher.data.local.database.AITeacherDatabase
import com.aiteacher.data.local.repository.StudentRepository
import com.aiteacher.presentation.navigation.AITeacherNavigation
import com.aiteacher.presentation.ui.theme.AITeacherTheme

class MainActivity : ComponentActivity() {
    
    // 数据库和仓库实例
    private lateinit var database: AITeacherDatabase
    private lateinit var studentRepository: StudentRepository
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 初始化数据库
        database = AITeacherDatabase.getDatabase(this)
        studentRepository = StudentRepository(database.studentDao())
        
        setContent {
            AITeacherTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    AITeacherNavigation(
                        navController = navController,
                        studentRepository = studentRepository
                    )
                }
            }
        }
    }
}
