package com.aiteacher.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.aiteacher.data.local.repository.StudentRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.GlobalScope
import org.koin.compose.getKoin
import com.aiteacher.presentation.ui.screens.LoginScreen
import com.aiteacher.presentation.ui.screens.HomeScreen
import com.aiteacher.presentation.ui.screens.HomeChatScreen
import com.aiteacher.presentation.ui.screens.ProfileScreen
import com.aiteacher.presentation.ui.screens.ParentDashboardScreen
import com.aiteacher.presentation.ui.screens.TeachingOutlineScreen
import com.aiteacher.presentation.ui.screens.LlmTestScreen
import com.aiteacher.presentation.ui.screens.TeachingScreen
import com.aiteacher.presentation.ui.screens.TestingScreen
import com.aiteacher.presentation.ui.screens.StatisticsScreen
import com.aiteacher.presentation.viewmodel.LearningViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun AITeacherNavigation(
    navController: NavHostController,
    studentRepository: StudentRepository,
    viewModel: com.aiteacher.presentation.viewmodel.MainViewModel = getKoin().get<com.aiteacher.presentation.viewmodel.MainViewModel>()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = { studentId, studentName, grade ->
                    // 先跳转到对话主页，然后异步设置学生信息
                    navController.navigate(Screen.HomeChat.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                    // 异步设置学生信息（在Composable外执行协程）
                    android.util.Log.d("AITeacherNavigation", "准备调用setCurrentStudentFromLogin: $studentId")
                    GlobalScope.launch(kotlinx.coroutines.Dispatchers.Main) {
                        viewModel.setCurrentStudentFromLogin(studentId, studentName, grade)
                    }
                },
                studentRepository = studentRepository
            )
        }
        
        // 新的对话主页
        composable(Screen.HomeChat.route) {
            HomeChatScreen(
                navController = navController,
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile.route)
                },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.HomeChat.route) { inclusive = true }
                    }
                }
            )
        }
        
        // 旧的主页（已注释，保留备用）
        /*
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToMath = {
                    // 从主页跳转到教学大纲页面
                    navController.navigate(Screen.TeachingOutline.route)
                },
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile.route)
                },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToLlmTest = {
                    navController.navigate(Screen.LlmTest.route)
                }
            )
        }
        */
        
        composable(Screen.Profile.route) {
            ProfileScreen(
                onBack = {
                    navController.popBackStack()
                },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        
        // 学科大纲界面（支持学科参数）
        composable(Screen.TeachingOutline.route) { backStackEntry ->
            val subject = backStackEntry.arguments?.getString("subject") ?: "数学"
            val studentId = backStackEntry.arguments?.getString("studentId") ?: ""
            
            TeachingOutlineScreen(
                onNavigateToLearning = { id, name, g ->
                    navController.navigate(Screen.Learning.createRoute(id, name, g))
                },
                onBackToHome = {
                    navController.navigate(Screen.HomeChat.route) {
                        popUpTo(Screen.TeachingOutline.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.LlmTest.route) {
            LlmTestScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }
        
        // 教学界面路由
        composable(Screen.Learning.route) { backStackEntry ->
            val studentId = backStackEntry.arguments?.getString("studentId") ?: ""
            val studentName = backStackEntry.arguments?.getString("studentName") ?: ""
            val grade = backStackEntry.arguments?.getString("grade")?.toIntOrNull() ?: 7
            
            // 获取LearningViewModel
            val viewModel: LearningViewModel = koinViewModel()
            val feedback by viewModel.feedback.collectAsState()
            
            // 显示教学界面
            TeachingScreen(
                task = viewModel.currentTeachingTask,
                feedback = feedback,
                onAnswerSubmit = { answer ->
                    viewModel.submitTeachingAnswer(answer)
                },
                onContinueNext = {
                    // 导航到检验界面
                    navController.navigate(Screen.Testing.createRoute(studentId, studentName, grade))
                }
            )
        }
        
        // 检验界面路由（支持学科参数）
        composable(Screen.Testing.route) { backStackEntry ->
            val subject = backStackEntry.arguments?.getString("subject") ?: "数学"
            val studentId = backStackEntry.arguments?.getString("studentId") ?: ""
            val studentName = backStackEntry.arguments?.getString("studentName") ?: ""
            val grade = backStackEntry.arguments?.getString("grade")?.toIntOrNull() ?: 7
            
            // 获取LearningViewModel
            val viewModel: LearningViewModel = koinViewModel()
            val feedback by viewModel.feedback.collectAsState()
            
            // 显示检验界面
            TestingScreen(
                testingTask = viewModel.currentTestingTask.copy(studentId = studentId),
                result = null,
                feedback = feedback,
                onAnswerSubmit = { answer, imageAnswer ->
                    viewModel.submitTestingAnswer(answer)
                },
                onBackHome = {
                    // 返回对话主页
                    navController.navigate(Screen.HomeChat.route) {
                        popUpTo(Screen.HomeChat.route) { inclusive = false }
                    }
                }
            )
        }
        
        // 学习统计界面（新增）
        composable(Screen.Statistics.route) {
            StatisticsScreen(
                onBack = {
                    navController.popBackStack()
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
                },
                studentRepository = studentRepository
            )
        }
    }
}
