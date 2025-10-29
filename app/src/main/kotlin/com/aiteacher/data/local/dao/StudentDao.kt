package com.aiteacher.data.local.dao

import androidx.room.*
import com.aiteacher.data.local.entity.StudentEntity
import kotlinx.coroutines.flow.Flow

/**
 * 学生数据访问对象 - 使用Room
 */
@Dao
interface StudentDao {
    
    @Query("SELECT * FROM students WHERE student_id = :studentId")
    suspend fun getStudentById(studentId: String): StudentEntity?
    
    @Query("SELECT * FROM students")
    suspend fun getAllStudents(): List<StudentEntity>
    
    @Query("SELECT * FROM students")
    fun getAllStudentsFlow(): Flow<List<StudentEntity>>
    
    @Query("SELECT * FROM students WHERE grade = :grade")
    suspend fun getStudentsByGrade(grade: Int): List<StudentEntity>
    
    @Query("SELECT * FROM students ORDER BY createdAt DESC LIMIT :limit")
    suspend fun getRecentStudents(limit: Int): List<StudentEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: StudentEntity)
    
    @Update
    suspend fun updateStudent(student: StudentEntity)
    
    @Delete
    suspend fun deleteStudent(student: StudentEntity)
    
    @Query("DELETE FROM students WHERE student_id = :studentId")
    suspend fun deleteStudentById(studentId: String)
    
    @Query("SELECT EXISTS(SELECT 1 FROM students WHERE student_id = :studentId)")
    suspend fun studentExists(studentId: String): Boolean
}
