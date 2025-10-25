package com.aiteacher

import android.app.Application
import com.aiteacher.data.local.database.AppDatabase
import com.aiteacher.data.local.repository.StudentRepository
import com.aiteacher.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

/**
 * Android应用程序类
 * 管理依赖注入和数据库连接
 */
class AITeacherApplication : Application() {
    
    // 为了兼容现有代码，保留studentRepository
    val studentRepository by lazy { 
        StudentRepository(AppDatabase.getDatabase(this).studentDao())
    }
    
    override fun onCreate() {
        super.onCreate()
        instance = this
        
        // 启动Koin
        startKoin {
            androidLogger()
            androidContext(this@AITeacherApplication)
            modules(appModule)
        }
        
        android.util.Log.d("AITeacherApplication", "=== Koin应用初始化成功 ===")
    }
    
    companion object {
        @Volatile
        private var instance: AITeacherApplication? = null
        
        fun getInstance(): AITeacherApplication {
            return instance ?: throw IllegalStateException("Application not initialized")
        }
    }
}