package com.aiteacher

import android.app.Application
import com.aiteacher.data.local.database.AITeacherDatabase
import com.aiteacher.data.local.repository.StudentRepository

class AITeacherApplication : Application() {
    
    // 简单的单例依赖管理
    val database by lazy { AITeacherDatabase.getDatabase(this) }
    val studentRepository by lazy { StudentRepository(database.studentDao()) }
    
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
