package com.aiteacher.data.local

import com.aiteacher.data.local.database.UserDatabase
import com.aiteacher.data.local.entity.UserType
import com.aiteacher.data.local.repository.UserRepository
import com.aiteacher.domain.model.Student
import com.aiteacher.domain.model.LearningProgress
import kotlinx.coroutines.runBlocking
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

/**
 * 用户数据库测试类
 * 测试用户数据库的基本功能
 */
@RunWith(AndroidJUnit4::class)
class UserDatabaseTest {
    
    private lateinit var database: UserDatabase
    private lateinit var userRepository: UserRepository
    
    @Before
    fun createDatabase() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = UserDatabase.getDatabase(context)
        userRepository = UserRepository(database.userDao())
    }
    
    @After
    @Throws(IOException::class)
    fun closeDatabase() {
        database.close()
    }
    
    @Test
    @Throws(Exception::class)
    fun testCreateAndGetUser() = runBlocking {
        // 创建学生用户
        val student = Student(
            studentId = "student_001",
            studentName = "张三",
            grade = 7,
            currentChapter = "第一章 有理数",
            learningProgress = LearningProgress(
                notTaught = listOf("7_1_1_1", "7_1_1_2"),
                taughtToReview = listOf("7_1_1_3"),
                notMastered = listOf("7_1_1_4"),
                basicMastery = listOf("7_1_1_5"),
                fullMastery = listOf("7_1_1_6"),
                lastUpdateTime = System.currentTimeMillis().toString()
            )
        )
        
        // 创建用户
        val createUserResult = userRepository.createUser(
            userId = "user_001",
            userType = UserType.STUDENT,
            studentInfo = student
        )
        
        assert(createUserResult.isSuccess)
        
        // 获取用户
        val user = userRepository.getUserById("user_001")
        assert(user != null)
        assert(user!!.userId == "user_001")
        assert(user.userType == UserType.STUDENT)
        assert(user.studentId == "student_001")
        assert(user.studentName == "张三")
        assert(user.grade == 7)
        assert(user.currentChapter == "第一章 有理数")
        
        // 创建家长用户
        val createParentResult = userRepository.createUser(
            userId = "parent_001",
            userType = UserType.PARENT
        )
        
        assert(createParentResult.isSuccess)
        
        // 获取家长用户
        val parentUser = userRepository.getUserById("parent_001")
        assert(parentUser != null)
        assert(parentUser!!.userId == "parent_001")
        assert(parentUser.userType == UserType.PARENT)
        assert(parentUser.studentId == null)
        assert(parentUser.studentName == null)
        assert(parentUser.grade == null)
        assert(parentUser.currentChapter == null)
    }
}