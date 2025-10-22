package com.aiteacher.data.local

import com.aiteacher.data.local.database.KnowledgeDatabase
import com.aiteacher.data.local.database.QuestionDatabase
import com.aiteacher.data.local.entity.KnowledgeEntity
import com.aiteacher.data.local.entity.QuestionEntity
import com.aiteacher.data.local.repository.KnowledgeRepository
import com.aiteacher.data.local.repository.QuestionRepository
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
 * 知识数据库测试类
 * 测试知识点和题目数据库的基本功能
 */
@RunWith(AndroidJUnit4::class)
class KnowledgeDatabaseTest {
    
    private lateinit var knowledgeDatabase: KnowledgeDatabase
    private lateinit var questionDatabase: QuestionDatabase
    private lateinit var knowledgeRepository: KnowledgeRepository
    private lateinit var questionRepository: QuestionRepository
    
    @Before
    fun createDatabase() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        knowledgeDatabase = KnowledgeDatabase.getDatabase(context)
        questionDatabase = QuestionDatabase.getDatabase(context)
        knowledgeRepository = KnowledgeRepository(knowledgeDatabase.knowledgeDao())
        questionRepository = QuestionRepository(questionDatabase.questionDao())
    }
    
    @After
    @Throws(IOException::class)
    fun closeDatabase() {
        knowledgeDatabase.close()
        questionDatabase.close()
    }
    
    @Test
    @Throws(Exception::class)
    fun testInsertAndGetKnowledge() = runBlocking {
        // 创建知识点
        val knowledge = KnowledgeEntity(
            knowledgeId = "M7-001",
            subject = "数学",
            grade = 7,
            chapter = "有理数",
            concept = "有理数是可以表示为两个整数之比的数，即形如 a/b（b≠0）的数。",
            applicationMethods = listOf(
                "比较大小：通分后比较分子",
                "加减法：先通分，再运算"
            ),
            keywords = listOf("有理数", "分数", "整数比")
        )
        
        // 插入知识点
        val insertResult = knowledgeRepository.insertKnowledge(knowledge)
        assert(insertResult.isSuccess)
        
        // 获取知识点
        val retrievedKnowledge = knowledgeRepository.getKnowledgeById("M7-001")
        assert(retrievedKnowledge != null)
        assert(retrievedKnowledge!!.knowledgeId == "M7-001")
        assert(retrievedKnowledge.subject == "数学")
        assert(retrievedKnowledge.grade == 7)
        assert(retrievedKnowledge.chapter == "有理数")
        assert(retrievedKnowledge.concept == "有理数是可以表示为两个整数之比的数，即形如 a/b（b≠0）的数。")
        assert(retrievedKnowledge.applicationMethods.size == 2)
        assert(retrievedKnowledge.keywords.size == 3)
    }
    
    @Test
    @Throws(Exception::class)
    fun testInsertAndGetQuestion() = runBlocking {
        // 创建题目
        val question = QuestionEntity(
            questionId = "Q1001",
            subject = "数学",
            grade = 7,
            questionText = "下列哪个数是有理数？A. √2  B. π  C. 3/4  D. e",
            answer = "C",
            questionType = "单选题",
            difficulty = 2,
            relatedKnowledgeIds = listOf("M7-001")
        )
        
        // 插入题目
        val insertResult = questionRepository.insertQuestion(question)
        assert(insertResult.isSuccess)
        
        // 获取题目
        val retrievedQuestion = questionRepository.getQuestionById("Q1001")
        assert(retrievedQuestion != null)
        assert(retrievedQuestion!!.questionId == "Q1001")
        assert(retrievedQuestion.subject == "数学")
        assert(retrievedQuestion.grade == 7)
        assert(retrievedQuestion.questionText == "下列哪个数是有理数？A. √2  B. π  C. 3/4  D. e")
        assert(retrievedQuestion.answer == "C")
        assert(retrievedQuestion.questionType == "单选题")
        assert(retrievedQuestion.difficulty == 2)
        assert(retrievedQuestion.relatedKnowledgeIds.size == 1)
        assert(retrievedQuestion.relatedKnowledgeIds[0] == "M7-001")
    }
    
    @Test
    @Throws(Exception::class)
    fun testGetQuestionsByKnowledgeId() = runBlocking {
        // 创建知识点
        val knowledge = KnowledgeEntity(
            knowledgeId = "M7-002",
            subject = "数学",
            grade = 7,
            chapter = "整数",
            concept = "整数是正整数、负整数和零的集合。",
            applicationMethods = listOf("加减法运算", "比较大小"),
            keywords = listOf("整数", "正整数", "负整数")
        )
        
        // 插入知识点
        knowledgeRepository.insertKnowledge(knowledge)
        
        // 创建多个相关题目
        val question1 = QuestionEntity(
            questionId = "Q1002",
            subject = "数学",
            grade = 7,
            questionText = "下列哪个是整数？A. 1.5  B. -3  C. 2/3  D. √2",
            answer = "B",
            questionType = "单选题",
            difficulty = 1,
            relatedKnowledgeIds = listOf("M7-002")
        )
        
        val question2 = QuestionEntity(
            questionId = "Q1003",
            subject = "数学",
            grade = 7,
            questionText = "整数包括哪些？A. 正整数  B. 负整数  C. 零  D. 以上都是",
            answer = "D",
            questionType = "单选题",
            difficulty = 1,
            relatedKnowledgeIds = listOf("M7-002")
        )
        
        // 插入题目
        questionRepository.insertQuestion(question1)
        questionRepository.insertQuestion(question2)
        
        // 根据知识点ID获取相关题目
        val questions = questionRepository.getQuestionsByKnowledgeId("M7-002")
        assert(questions.size == 2)
    }
}