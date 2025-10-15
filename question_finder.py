#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import json
import logging
from typing import List, Dict, Any, Optional
import sqlite3
from contextlib import contextmanager

# 配置日志
logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(name)s - %(levelname)s - %(message)s')
logger = logging.getLogger(__name__)


class DatabaseManager:
    """
    数据库管理类，用于连接和操作云端SQL数据库
    """
    
    def __init__(self, db_host: str = "localhost", db_port: int = 5432, 
                 db_name: str = "aiteacher", db_user: str = "user", db_password: str = "password"):
        """
        初始化数据库管理器
        
        Args:
            db_host: 数据库主机地址
            db_port: 数据库端口
            db_name: 数据库名称
            db_user: 数据库用户名
            db_password: 数据库密码
        """
        self.db_host = db_host
        self.db_port = db_port
        self.db_name = db_name
        self.db_user = db_user
        self.db_password = db_password
        # 在实际实现中，这里应该建立数据库连接池
        # 但目前我们使用模拟数据进行演示
        
    @contextmanager
    def get_db_connection(self):
        """
        获取数据库连接的上下文管理器
        """
        # 在实际实现中，这里应该创建真实的数据库连接
        # 目前我们返回一个模拟连接用于演示
        conn = MockDatabaseConnection()
        try:
            yield conn
        finally:
            conn.close()


class MockDatabaseConnection:
    """
    模拟数据库连接类，用于演示目的
    在实际实现中，这应该被真实的数据库连接替代
    """
    
    def cursor(self):
        return MockCursor()
    
    def close(self):
        pass


class MockCursor:
    """
    模拟数据库游标类，用于演示目的
    """
    
    def execute(self, query: str, params: tuple = ()):
        self.query = query
        self.params = params
        # 解析查询以确定返回什么数据
        if "KnowledgeBase" in query and "question_id" not in query:
            # 查询知识点表
            self.result = [
                {
                    "knowledge_id": "M7-001",
                    "subject": "数学",
                    "grade": 7,
                    "chapter": "有理数",
                    "concept": "有理数是可以表示为两个整数之比的数，即形如 a/b（b≠0）的数。",
                    "application_methods": json.dumps([
                        "比较大小：通分后比较分子",
                        "加减法：先通分，再运算"
                    ]),
                    "keywords": json.dumps(["有理数", "分数", "整数比"])
                }
            ]
        elif "QuestionBank" in query:
            # 查询题库表
            self.result = [
                {
                    "question_id": "Q1001",
                    "subject": "数学",
                    "grade": 7,
                    "question_text": "下列哪个数是有理数？A. √2  B. π  C. 3/4  D. e",
                    "answer": "C",
                    "question_type": "单选题",
                    "difficulty": 2,
                    "related_knowledge_ids": json.dumps(["M7-001"])
                },
                {
                    "question_id": "Q1002",
                    "subject": "数学",
                    "grade": 7,
                    "question_text": "以下哪个数不是有理数？A. 1/2  B. -3  C. 0  D. √3",
                    "answer": "D",
                    "question_type": "单选题",
                    "difficulty": 3,
                    "related_knowledge_ids": json.dumps(["M7-001"])
                },
                {
                    "question_id": "Q1003",
                    "subject": "数学",
                    "grade": 7,
                    "question_text": "计算: 1/4 + 1/6 = ? A. 5/12  B. 2/10  C. 1/24  D. 2/5",
                    "answer": "A",
                    "question_type": "单选题",
                    "difficulty": 2,
                    "related_knowledge_ids": json.dumps(["M7-001"])
                }
            ]
        else:
            self.result = []
    
    def fetchall(self):
        return self.result
    
    def close(self):
        pass


class QuestionFinder:
    """
    根据知识点查找相关试题的工具类
    """

    def __init__(self, db_manager: DatabaseManager = None):
        """
        初始化试题查找器
        
        Args:
            db_manager: 数据库管理器实例
        """
        self.db_manager = db_manager or DatabaseManager()
        logger.info("QuestionFinder 初始化完成，使用数据库连接")

    def find_questions_by_knowledge_id(self, knowledge_id: str) -> List[Dict[str, Any]]:
        """
        根据知识点ID查找相关试题
        
        Args:
            knowledge_id (str): 知识点ID
            
        Returns:
            List[Dict[str, Any]]: 相关试题列表
        """
        try:
            with self.db_manager.get_db_connection() as conn:
                cursor = conn.cursor()
                
                # 查询与知识点ID关联的题目
                query = """
                SELECT qb.* 
                FROM QuestionBank qb
                WHERE JSON_EXTRACT(qb.related_knowledge_ids, '$[*]') LIKE ?
                """
                # 注意：实际的SQL语法可能因数据库而异，这里使用的是通用形式
                # 在实际实现中，应该根据具体数据库调整查询语句
                cursor.execute(query, (f'%{knowledge_id}%',))
                results = cursor.fetchall()
                
                # 处理结果
                questions = []
                for row in results:
                    # 处理JSON字段
                    question = dict(row)
                    if 'related_knowledge_ids' in question:
                        try:
                            question['related_knowledge_ids'] = json.loads(question['related_knowledge_ids'])
                        except:
                            question['related_knowledge_ids'] = []
                    
                    questions.append(question)
                
                logger.info(f"根据知识点ID '{knowledge_id}' 找到了 {len(questions)} 道相关试题")
                return questions
                
        except Exception as e:
            logger.error(f"根据知识点ID查找试题时发生错误: {e}")
            return []

    def find_questions_by_concept(self, concept: str) -> List[Dict[str, Any]]:
        """
        根据知识点概念名称查找相关试题
        
        Args:
            concept (str): 知识点概念名称
            
        Returns:
            List[Dict[str, Any]]: 相关试题列表
        """
        try:
            with self.db_manager.get_db_connection() as conn:
                cursor = conn.cursor()
                
                # 首先查找匹配的知识点
                knowledge_query = """
                SELECT * FROM KnowledgeBase 
                WHERE concept LIKE ? OR chapter LIKE ? OR JSON_EXTRACT(keywords, '$[*]') LIKE ?
                """
                cursor.execute(knowledge_query, (f'%{concept}%', f'%{concept}%', f'%{concept}%'))
                knowledge_results = cursor.fetchall()
                
                # 收集匹配的知识点ID
                knowledge_ids = [row['knowledge_id'] for row in knowledge_results]
                
                if not knowledge_ids:
                    return []
                
                # 构建查找与这些知识点ID关联的试题的查询
                placeholders = ','.join(['?' for _ in knowledge_ids])
                question_query = f"""
                SELECT * FROM QuestionBank 
                WHERE JSON_EXTRACT(related_knowledge_ids, '$[*]') IN ({placeholders})
                """
                cursor.execute(question_query, knowledge_ids)
                question_results = cursor.fetchall()
                
                # 处理结果
                questions = []
                for row in question_results:
                    # 处理JSON字段
                    question = dict(row)
                    if 'related_knowledge_ids' in question:
                        try:
                            question['related_knowledge_ids'] = json.loads(question['related_knowledge_ids'])
                        except:
                            question['related_knowledge_ids'] = []
                    
                    questions.append(question)
                
                # 去重（以防题目关联了多个匹配的知识点）
                unique_questions = []
                seen_ids = set()
                for question in questions:
                    if question["question_id"] not in seen_ids:
                        unique_questions.append(question)
                        seen_ids.add(question["question_id"])
                
                logger.info(f"根据概念 '{concept}' 找到了 {len(unique_questions)} 道相关试题")
                return unique_questions
                
        except Exception as e:
            logger.error(f"根据概念查找试题时发生错误: {e}")
            return []

    def find_questions_by_subject_and_grade(self, subject: str, grade: int) -> List[Dict[str, Any]]:
        """
        根据学科和年级查找试题
        
        Args:
            subject (str): 学科
            grade (int): 年级
            
        Returns:
            List[Dict[str, Any]]: 相关试题列表
        """
        try:
            with self.db_manager.get_db_connection() as conn:
                cursor = conn.cursor()
                
                # 查询匹配学科和年级的题目
                query = "SELECT * FROM QuestionBank WHERE subject = ? AND grade = ?"
                cursor.execute(query, (subject, grade))
                results = cursor.fetchall()
                
                # 处理结果
                questions = []
                for row in results:
                    # 处理JSON字段
                    question = dict(row)
                    if 'related_knowledge_ids' in question:
                        try:
                            question['related_knowledge_ids'] = json.loads(question['related_knowledge_ids'])
                        except:
                            question['related_knowledge_ids'] = []
                    
                    questions.append(question)
                
                logger.info(f"根据学科 '{subject}' 和年级 '{grade}' 找到了 {len(questions)} 道试题")
                return questions
                
        except Exception as e:
            logger.error(f"根据学科和年级查找试题时发生错误: {e}")
            return []


def create_find_questions_by_knowledge_tool():
    """
    创建根据知识点查找相关试题的工具
    
    Returns:
        base_tool: 可以被agent使用的工具对象
    """
    try:
        # 动态导入utils模块以避免循环依赖
        import utils
        
        # 创建工具
        find_questions_tool = utils.base_tool(
            tool_name="find_questions_by_knowledge",
            tool_description="根据知识点ID或概念名称查找相关试题",
            parameters={
                "type": "object",
                "properties": {
                    "knowledge_id": {
                        "type": "string",
                        "description": "知识点ID（如M7-001），与concept参数二选一"
                    },
                    "concept": {
                        "type": "string",
                        "description": "知识点概念名称（如有理数），与knowledge_id参数二选一"
                    },
                    "subject": {
                        "type": "string",
                        "description": "学科名称（如数学、物理），可选参数"
                    },
                    "grade": {
                        "type": "integer",
                        "description": "年级（如7表示七年级），可选参数"
                    }
                },
                "required": []
            }
        )
        
        # 创建QuestionFinder实例，使用云端数据库
        # 这里使用示例数据库连接参数，实际使用时应该替换为真实的数据库连接信息
        db_manager = DatabaseManager(
            db_host="cloud-db.example.com",
            db_port=5432,  # 使用题目中提到的示例端口
            db_name="aiteacher",
            db_user="ai_teacher_user",
            db_password="secure_password"
        )
        question_finder = QuestionFinder(db_manager)
        
        def find_questions_func(knowledge_id: str = None, concept: str = None, 
                               subject: str = None, grade: int = None) -> str:
            """
            根据知识点查找相关试题的函数
            
            Args:
                knowledge_id (str): 知识点ID
                concept (str): 知识点概念名称
                subject (str): 学科名称
                grade (int): 年级
                
            Returns:
                str: 试题信息的JSON字符串
            """
            try:
                # 根据提供的参数选择查找方式
                if knowledge_id:
                    questions = question_finder.find_questions_by_knowledge_id(knowledge_id)
                elif concept:
                    questions = question_finder.find_questions_by_concept(concept)
                elif subject and grade:
                    questions = question_finder.find_questions_by_subject_and_grade(subject, grade)
                else:
                    return "错误：必须提供knowledge_id、concept或者subject和grade参数"
                
                if not questions:
                    return "未找到相关试题"
                
                # 格式化返回结果
                result = {
                    "total_count": len(questions),
                    "questions": questions
                }
                
                return json.dumps(result, ensure_ascii=False, indent=2)
                
            except Exception as e:
                logger.error(f"查找试题过程中发生错误: {e}")
                return f"查找试题过程中发生错误: {str(e)}"
        
        find_questions_tool.set_function(find_questions_func)
        return find_questions_tool
        
    except ImportError as e:
        logger.error(f"导入utils模块失败: {e}")
        raise
    except Exception as e:
        logger.error(f"创建查找试题工具时发生错误: {e}")
        raise


# 测试代码
if __name__ == "__main__":
    # 创建查找器实例，使用云端数据库
    db_manager = DatabaseManager(
        db_host="cloud-db.example.com",
        db_port=5432,  # 使用题目中提到的示例端口
        db_name="aiteacher",
        db_user="ai_teacher_user",
        db_password="secure_password"
    )
    finder = QuestionFinder(db_manager)
    
    # 测试根据知识点ID查找试题
    print("根据知识点ID 'M7-001' 查找试题:")
    questions = finder.find_questions_by_knowledge_id("M7-001")
    for q in questions:
        print(f"  题目: {q['question_text']}")
        print(f"  答案: {q['answer']}")
        print()
    
    # 测试根据概念查找试题
    print("根据概念 '有理数' 查找试题:")
    questions = finder.find_questions_by_concept("有理数")
    for q in questions:
        print(f"  题目: {q['question_text']}")
        print(f"  答案: {q['answer']}")
        print()