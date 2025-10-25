package com.aiteacher.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.aiteacher.data.local.repository.StudentRepository
import com.aiteacher.presentation.ui.screens.LoginScreen
import com.aiteacher.presentation.ui.screens.HomeScreen
import com.aiteacher.presentation.ui.screens.ProfileScreen
import com.aiteacher.presentation.ui.screens.LearningScreen
import com.aiteacher.presentation.ui.screens.ParentDashboardScreen
import com.aiteacher.presentation.ui.screens.TeachingOutlineScreen

@Composable
fun AITeacherNavigation(
    navController: NavHostController,
    studentRepository: StudentRepository
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = { studentId, studentName, grade ->
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                studentRepository = studentRepository
            )
        }
        
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToMath = {
                    // 从主页跳转到教学大纲页面
                    navController.navigate(Screen.TeachingOutline.createRoute("student_default", "默认学生", 7))
                },
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile.route)
                },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Profile.route) {
            ProfileScreen(
                onBack = {
                    navController.popBackStack()
                },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.TeachingOutline.route) { backStackEntry ->
            val studentId = backStackEntry.arguments?.getString("studentId") ?: ""
            val studentName = backStackEntry.arguments?.getString("studentName") ?: ""
            val grade = backStackEntry.arguments?.getString("grade")?.toIntOrNull() ?: 7
            
            TeachingOutlineScreen(
                studentId = studentId,
                studentName = studentName,
                grade = grade,
                onNavigateToLearning = { id, name, g ->
                    navController.navigate(Screen.Learning.createRoute(id, name, g))
                },
                onBackToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.TeachingOutline.route) { inclusive = true }
                    }
                },
                studentRepository = studentRepository
            )
        }
        
        composable(Screen.Learning.route) { backStackEntry ->
            val studentId = backStackEntry.arguments?.getString("studentId") ?: ""
            val studentName = backStackEntry.arguments?.getString("studentName") ?: ""
            val grade = backStackEntry.arguments?.getString("grade")?.toIntOrNull() ?: 7
            
            LearningScreen(
                studentId = studentId,
                studentName = studentName,
                grade = grade,
                onNavigateToParent = {
                    navController.navigate(Screen.ParentDashboard.createRoute(studentId, studentName))
                },
                onBackToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Learning.route) { inclusive = true }
                    }
                },
                studentRepository = studentRepository
            )
        }
        
        composable(Screen.ParentDashboard.route) { backStackEntry ->
            val studentId = backStackEntry.arguments?.getString("studentId") ?: ""
            val studentName = backStackEntry.arguments?.getString("studentName") ?: ""
            
            ParentDashboardScreen(
                studentId = studentId,
                studentName = studentName,
                onNavigateBack = {
                    navController.popBackStack()
                },
                studentRepository = studentRepository
            )
        }
    }
}
