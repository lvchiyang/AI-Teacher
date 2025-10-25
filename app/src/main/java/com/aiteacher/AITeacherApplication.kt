package com.aiteacher

import android.app.Application
import com.aiteacher.data.local.database.KnowledgeDatabase
import com.aiteacher.data.local.database.QuestionDatabase
import com.aiteacher.data.local.database.UserDatabase
import com.aiteacher.data.local.database.AITeacherDatabase
import com.aiteacher.data.local.repository.KnowledgeRepository
import com.aiteacher.data.local.repository.QuestionRepository
import com.aiteacher.data.local.repository.StudentRepository
import com.aiteacher.data.local.repository.UserRepository

class AITeacherApplication : Application() {
    
    // 简单的单例依赖管理
    val studentDatabase by lazy { AITeacherDatabase.getDatabase(this) }
    val userDatabase by lazy { UserDatabase.getDatabase(this) }
    val knowledgeDatabase by lazy { KnowledgeDatabase.getDatabase(this) }
    val questionDatabase by lazy { QuestionDatabase.getDatabase(this) }
    
    val studentRepository by lazy { StudentRepository(studentDatabase.studentDao()) }
    val userRepository by lazy { UserRepository(userDatabase.userDao()) }
    val knowledgeRepository by lazy { KnowledgeRepository(knowledgeDatabase.knowledgeDao()) }
    val questionRepository by lazy { QuestionRepository(questionDatabase.questionDao()) }
    
    companion object {
        @Volatile
        private var INSTANCE: AITeacherApplication? = null
        
        fun getInstance(): AITeacherApplication {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AITeacherApplication().also { INSTANCE = it }
            }
        }
    }
}