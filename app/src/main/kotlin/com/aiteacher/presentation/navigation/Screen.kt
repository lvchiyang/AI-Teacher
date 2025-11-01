package com.aiteacher.presentation.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Home : Screen("home")  // 旧主页（已注释，保留备用）
    object HomeChat : Screen("home_chat")  // 新的对话主页
    object Profile : Screen("profile")
    
    // 学科大纲界面（学习入口）- 支持学科参数
    object TeachingOutline : Screen("teaching_outline/{subject}/{studentId}") {
        fun createRoute(subject: String, studentId: String) = 
            "teaching_outline/$subject/$studentId"
    }
    
    object LlmTest : Screen("llm_test")
    
    // 教学流程界面（从大纲进入）
    object Learning : Screen("learning/{studentId}/{studentName}/{grade}") {
        fun createRoute(studentId: String, studentName: String, grade: Int) = 
            "learning/$studentId/$studentName/$grade"
    }
    
    // 检验/做题界面（独立入口）- 支持学科参数
    object Testing : Screen("testing/{subject}/{studentId}/{studentName}/{grade}") {
        // 原有方法（保留兼容）
        fun createRoute(studentId: String, studentName: String, grade: Int) = 
            "testing/math/$studentId/$studentName/$grade"
        
        // 新方法（支持指定学科）
        fun createRoute(subject: String, studentId: String, studentName: String, grade: Int) = 
            "testing/$subject/$studentId/$studentName/$grade"
    }
    
    // 学习统计界面（新增）
    object Statistics : Screen("statistics")
    
    object ParentDashboard : Screen("parent_dashboard/{studentId}/{studentName}") {
        fun createRoute(studentId: String, studentName: String) = 
            "parent_dashboard/$studentId/$studentName"
    }
}
