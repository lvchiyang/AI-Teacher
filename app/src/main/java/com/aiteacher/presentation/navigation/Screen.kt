package com.aiteacher.presentation.navigation

sealed class Screen(val route: String) {
    object Welcome : Screen("welcome")
    object Learning : Screen("learning/{studentId}/{studentName}/{grade}") {
        fun createRoute(studentId: String, studentName: String, grade: Int) = 
            "learning/$studentId/$studentName/$grade"
    }
    object ParentDashboard : Screen("parent_dashboard/{studentId}/{studentName}") {
        fun createRoute(studentId: String, studentName: String) = 
            "parent_dashboard/$studentId/$studentName"
    }
}
