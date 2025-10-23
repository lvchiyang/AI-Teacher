package com.aiteacher.presentation.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aiteacher.data.local.repository.StudentRepository
import com.aiteacher.domain.model.Student
import com.aiteacher.domain.usecase.StudentUseCase
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: (String, String, Int) -> Unit,
    studentRepository: StudentRepository
) {
    var studentName by remember { mutableStateOf("") }
    var grade by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var existingStudent by remember { mutableStateOf<Student?>(null) }
    var isFirstTime by remember { mutableStateOf(true) }
    
    val studentUseCase = remember { StudentUseCase(studentRepository) }
    val scope = rememberCoroutineScope()
    
    // 检查是否有已存在的学生
    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val students = studentUseCase.getAllStudents()
                students.fold(
                    onSuccess = { studentList ->
                        if (studentList.isNotEmpty()) {
                            // 如果有学生，显示第一个学生的信息
                            val student = studentList.first()
                            existingStudent = student
                            studentName = student.studentName
                            grade = student.grade.toString()
                            isFirstTime = false
                        }
                    },
                    onFailure = { }
                )
            } catch (e: Exception) {
                // 忽略错误，继续显示输入界面
            }
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "AI 智能教师",
            fontSize = 32.sp,
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = 48.dp)
        )

        // 如果有已存在的学生，显示学生信息
        if (!isFirstTime && existingStudent != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "欢迎回来！",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = "学生：${existingStudent!!.studentName}",
                        fontSize = 16.sp,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(
                        text = "年级：${existingStudent!!.grade}年级",
                        fontSize = 16.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    Text(
                        text = "当前章节：${existingStudent!!.currentChapter}",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        OutlinedTextField(
            value = studentName,
            onValueChange = { studentName = it },
            label = { Text("学生姓名") },
            singleLine = true,
            enabled = isFirstTime,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = grade,
            onValueChange = { if (it.all { char -> char.isDigit() }) grade = it },
            label = { Text("年级") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            enabled = isFirstTime,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        )

        // 错误信息
        if (errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        Button(
            onClick = {
                if (studentName.isNotBlank() && grade.isNotBlank()) {
                    isLoading = true
                    errorMessage = ""
                    
                    scope.launch {
                        try {
                            val studentId = "student_${studentName.hashCode()}"
                            val gradeInt = grade.toInt()
                            
                            if (isFirstTime) {
                                // 第一次登录，创建新学生
                                val result = studentUseCase.createStudent(studentId, studentName, gradeInt)
                                result.fold(
                                    onSuccess = { student ->
                                        onLoginSuccess(studentId, studentName, gradeInt)
                                    },
                                    onFailure = { exception ->
                                        errorMessage = "登录失败：${exception.message}"
                                        isLoading = false
                                    }
                                )
                            } else {
                                // 已有学生，直接登录
                                onLoginSuccess(studentId, studentName, gradeInt)
                            }
                        } catch (e: Exception) {
                            errorMessage = "登录失败：${e.message}"
                            isLoading = false
                        }
                    }
                } else {
                    errorMessage = "请输入学生姓名和年级"
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = studentName.isNotBlank() && grade.isNotBlank() && !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text(
                    text = if (isFirstTime) "登录 / 开始学习" else "继续学习",
                    fontSize = 18.sp
                )
            }
        }
        
        // 如果是已存在学生，提供重新输入选项
        if (!isFirstTime) {
            TextButton(
                onClick = {
                    isFirstTime = true
                    studentName = ""
                    grade = ""
                    existingStudent = null
                },
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text("使用其他学生信息")
            }
        }
    }
}
