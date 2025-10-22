package com.aiteacher.data.local.repository

import com.aiteacher.data.local.entity.UserType
import com.aiteacher.domain.model.Student
import com.aiteacher.domain.model.LearningProgress

/**
 * UserRepository 使用示例
 */
class UserRepositoryExample {
    
    /**
     * 创建学生用户的示例
     */
    suspend fun createStudentUserExample(userRepository: UserRepository) {
        // 创建学生信息
        val student = Student(
            studentId = "student_001",
            studentName = "张三",
            grade = 7,
            currentChapter = "第一章 有理数",
            learningProgress = LearningProgress(
                notTaught = listOf("7_1_1_1", "7_1_1_2"),
                taughtToReview = listOf("7_1_1_3"),
                notMastered = listOf("7_1_1_4"),
                basicMastery = listOf("7_1_1_5"),
                fullMastery = listOf("7_1_1_6"),
                lastUpdateTime = System.currentTimeMillis().toString()
            )
        )
        
        // 创建学生用户
        val result = userRepository.createUser(
            userId = "user_001",
            userType = UserType.STUDENT,
            studentInfo = student
        )
        
        if (result.isSuccess) {
            println("成功创建学生用户: ${result.getOrNull()}")
        } else {
            println("创建学生用户失败: ${result.exceptionOrNull()?.message}")
        }
    }
    
    /**
     * 创建家长用户的示例
     */
    suspend fun createParentUserExample(userRepository: UserRepository) {
        // 创建家长用户（不需要学生信息）
        val result = userRepository.createUser(
            userId = "parent_001",
            userType = UserType.PARENT
        )
        
        if (result.isSuccess) {
            println("成功创建家长用户: ${result.getOrNull()}")
        } else {
            println("创建家长用户失败: ${result.exceptionOrNull()?.message}")
        }
    }
    
    /**
     * 查询用户信息的示例
     */
    suspend fun getUserExample(userRepository: UserRepository) {
        val user = userRepository.getUserById("user_001")
        if (user != null) {
            println("找到用户: $user")
            
            // 根据用户类型处理不同逻辑
            when (user.userType) {
                UserType.STUDENT -> {
                    println("这是一个学生用户")
                    println("学生ID: ${user.studentId}")
                    println("学生姓名: ${user.studentName}")
                    println("年级: ${user.grade}")
                    println("当前章节: ${user.currentChapter}")
                }
                UserType.PARENT -> {
                    println("这是一个家长用户")
                }
            }
        } else {
            println("未找到用户")
        }
    }
}