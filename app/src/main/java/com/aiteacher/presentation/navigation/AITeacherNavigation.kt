package com.aiteacher.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.aiteacher.presentation.ui.screens.LoginScreen
import com.aiteacher.presentation.ui.screens.HomeScreen
import com.aiteacher.presentation.ui.screens.ProfileScreen
import com.aiteacher.presentation.ui.screens.WelcomeScreen
import com.aiteacher.presentation.ui.screens.LearningScreen
import com.aiteacher.presentation.ui.screens.ParentDashboardScreen

@Composable
fun AITeacherNavigation(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToMath = {
                    navController.navigate(Screen.Welcome.route)
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
        
        composable(Screen.Welcome.route) {
            WelcomeScreen(
                navController = navController,
                onBackToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Learning.route) { backStackEntry ->
            val studentId = backStackEntry.arguments?.getString("studentId") ?: ""
            val studentName = backStackEntry.arguments?.getString("studentName") ?: ""
            val grade = backStackEntry.arguments?.getString("grade")?.toIntOrNull() ?: 7
            
            LearningScreen(
                studentId = studentId,
                onNavigateToParent = {
                    navController.navigate(Screen.ParentDashboard.createRoute(studentId, studentName))
                },
                onBackToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Learning.route) { inclusive = true }
                    }
                }
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
                }
            )
        }
    }
}
