package com.aiteacher.domain.usecase

/**
 * GetTestingTaskUseCase 使用示例
 */
class GetTestingTaskUseCaseExample {
    // 这个类仅用于演示如何使用GetTestingTaskUseCase
    
    /*
    suspend fun exampleUsage() {
        // 创建UseCase实例（依赖项将通过Koin自动注入）
        val getTestingTaskUseCase = GetTestingTaskUseCase()
        
        // 示例1: 根据TaskId获取测试任务
        val taskId = "TT-1234567890"
        val taskResult = getTestingTaskUseCase(taskId)
        
        if (taskResult.isSuccess) {
            val task = taskResult.getOrNull()
            if (task != null) {
                println("找到测试任务: ${task.title}")
            } else {
                println("未找到指定的测试任务")
            }
        } else {
            println("获取测试任务失败: ${taskResult.exceptionOrNull()?.message}")
        }
        
        // 示例2: 根据StudentId获取最近的未完成测试任务
        val studentId = "S001"
        val latestIncompleteTaskResult = getTestingTaskUseCase.getLatestIncompleteTaskByStudentId(studentId)
        
        if (latestIncompleteTaskResult.isSuccess) {
            val task = latestIncompleteTaskResult.getOrNull()
            if (task != null) {
                println("找到最近的未完成任务: ${task.title}")
            } else {
                println("该学生没有未完成的测试任务")
            }
        } else {
            println("获取未完成测试任务失败: ${latestIncompleteTaskResult.exceptionOrNull()?.message}")
        }
        
        // 示例3: 根据StudentId获取所有未完成的测试任务
        val allIncompleteTasksResult = getTestingTaskUseCase.getAllIncompleteTasksByStudentId(studentId)
        
        if (allIncompleteTasksResult.isSuccess) {
            val tasks = allIncompleteTasksResult.getOrNull() ?: emptyList()
            println("找到 ${tasks.size} 个未完成的任务")
            tasks.forEach { task ->
                println("- ${task.title}")
            }
        } else {
            println("获取所有未完成测试任务失败: ${allIncompleteTasksResult.exceptionOrNull()?.message}")
        }
    }
    */
}