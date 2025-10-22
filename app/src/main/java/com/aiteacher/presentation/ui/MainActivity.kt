package com.aiteacher.presentation.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.aiteacher.AITeacherApplication
import com.aiteacher.presentation.navigation.AITeacherNavigation
import com.aiteacher.presentation.ui.theme.AITeacherTheme

class MainActivity : ComponentActivity() {
    
    private val studentRepository by lazy { 
        (application as AITeacherApplication).studentRepository 
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
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
