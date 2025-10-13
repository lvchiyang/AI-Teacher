package com.aiteacher.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class AITeacherApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        // 初始化应用
    }
}
