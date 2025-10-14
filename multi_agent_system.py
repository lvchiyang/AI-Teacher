#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import sys
import os
import json
import logging

# 将项目根目录添加到Python路径中
sys.path.append(os.path.join(os.path.dirname(__file__)))

import utils

# 配置日志
logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(name)s - %(levelname)s - %(message)s')
logger = logging.getLogger(__name__)

class MultiAgentSystem:
    """
    多Agent系统主程序
    实现多个专门Agent协同工作，完成用户任务
    """
    
    def __init__(self):
        """
        初始化多Agent系统
        """
        self.agents = {}
        self.create_agents()
        
    def create_agents(self):
        """
        创建各种专门的Agent
        """
        # 创建教学Agent - 负责教学任务
        teaching_agent = self.create_teaching_agent()
        self.agents["teaching"] = teaching_agent
        
        # 创建检验Agent - 负责出题和检验学习效果
        testing_agent = self.create_testing_agent()
        self.agents["testing"] = testing_agent
        
        # 创建教秘Agent - 负责整体教学计划和进度管理
        secretary_agent = self.create_secretary_agent()
        self.agents["secretary"] = secretary_agent
        
        # 创建家长Agent - 负责向家长报告学习情况
        parent_agent = self.create_parent_agent()
        self.agents["parent"] = parent_agent
        
        logger.info(f"创建了 {len(self.agents)} 个Agent: {list(self.agents.keys())}")
    
    def create_teaching_agent(self):
        """
        创建教学Agent
        """
        # 创建教学工具
        explain_concept_tool = utils.base_tool(
            tool_name="explain_concept",
            tool_description="解释数学概念",
            parameters={
                "type": "object",
                "properties": {
                    "concept": {
                        "type": "string",
                        "description": "需要解释的数学概念，如'勾股定理'、'一元二次方程'"
                    },
                    "difficulty": {
                        "type": "string",
                        "description": "解释难度等级: 初级、中级、高级",
                        "enum": ["初级", "中级", "高级"]
                    }
                },
                "required": ["concept"]
            }
        )
        
        def explain_concept_func(concept: str, difficulty: str = "中级") -> str:
            explanations = {
                "勾股定理": f"勾股定理({difficulty}): 直角三角形两条直角边的平方和等于斜边的平方。公式为: a² + b² = c²",
                "一元二次方程": f"一元二次方程({difficulty}): 只含有一个未知数，并且未知数的最高次数是二次的整式方程。一般形式为: ax² + bx + c = 0 (a≠0)",
                "相似三角形": f"相似三角形({difficulty}): 两个三角形对应角相等，对应边成比例"
            }
            return explanations.get(concept, f"关于'{concept}'的{difficulty}解释: 这是一个重要的数学概念。")
        
        explain_concept_tool.set_function(explain_concept_func)
        
        # 创建出例题工具
        give_example_tool = utils.base_tool(
            tool_name="give_example",
            tool_description="给出数学例题",
            parameters={
                "type": "object",
                "properties": {
                    "concept": {
                        "type": "string",
                        "description": "相关数学概念"
                    },
                    "difficulty": {
                        "type": "string",
                        "description": "题目难度等级: 简单、中等、困难",
                        "enum": ["简单", "中等", "困难"]
                    }
                },
                "required": ["concept"]
            }
        )
        
        def give_example_func(concept: str, difficulty: str = "中等") -> str:
            examples = {
                "勾股定理": f"{difficulty}例题: 已知直角三角形的两条直角边分别为3cm和4cm，求斜边长度? 答案: 5cm",
                "一元二次方程": f"{difficulty}例题: 解方程 x² - 5x + 6 = 0。答案: x₁=2, x₂=3"
            }
            return examples.get(concept, f"关于'{concept}'的{difficulty}例题: 请解决相关问题。")
        
        give_example_tool.set_function(give_example_func)
        
        # 创建教学Agent
        teaching_agent = utils.base_agent(
            name="TeachingAgent",
            description="教学代理，负责知识点讲解和例题演示",
            model=utils.llm_model("qwen-plus"),
            tools=[explain_concept_tool, give_example_tool],
            max_tool_iterations=3
        )
        
        return teaching_agent
    
    def create_testing_agent(self):
        """
        创建检验Agent
        """
        # 创建出题工具
        generate_question_tool = utils.base_tool(
            tool_name="generate_question",
            tool_description="生成数学练习题",
            parameters={
                "type": "object",
                "properties": {
                    "topic": {
                        "type": "string",
                        "description": "题目主题"
                    },
                    "count": {
                        "type": "integer",
                        "description": "题目数量",
                        "minimum": 1,
                        "maximum": 10
                    }
                },
                "required": ["topic", "count"]
            }
        )
        
        def generate_question_func(topic: str, count: int = 1) -> str:
            questions = {
                "勾股定理": [
                    "已知直角三角形的两条直角边分别为6cm和8cm，求斜边长度?",
                    "直角三角形的斜边长为10cm，一条直角边长为6cm，求另一条直角边长?",
                    "判断以下哪组数能构成直角三角形的三边长: A. 3,4,5  B. 1,2,3  C. 5,12,13"
                ],
                "一元二次方程": [
                    "解方程: x² - 7x + 12 = 0",
                    "解方程: 2x² - 5x + 2 = 0",
                    "已知一元二次方程x² - 3x + k = 0有一个根为1，求k的值"
                ]
            }
            
            topic_questions = questions.get(topic, [f"关于{topic}的练习题，请解答相关问题。"])
            selected = topic_questions[:min(count, len(topic_questions))]
            return "\n".join([f"{i+1}. {q}" for i, q in enumerate(selected)])
        
        generate_question_tool.set_function(generate_question_func)
        
        # 创建评判答案工具
        evaluate_answer_tool = utils.base_tool(
            tool_name="evaluate_answer",
            tool_description="评判用户答案的正确性",
            parameters={
                "type": "object",
                "properties": {
                    "question": {
                        "type": "string",
                        "description": "题目内容"
                    },
                    "user_answer": {
                        "type": "string",
                        "description": "用户答案"
                    },
                    "correct_answer": {
                        "type": "string",
                        "description": "正确答案"
                    }
                },
                "required": ["question", "user_answer", "correct_answer"]
            }
        )
        
        def evaluate_answer_func(question: str, user_answer: str, correct_answer: str) -> str:
            # 简单的字符串匹配评判，实际应用中可以更复杂
            is_correct = user_answer.strip().lower() == correct_answer.strip().lower()
            return f"{'正确' if is_correct else '错误'}。{'很好!' if is_correct else f'正确答案是: {correct_answer}'}"
        
        evaluate_answer_tool.set_function(evaluate_answer_func)
        
        # 创建检验Agent
        testing_agent = utils.base_agent(
            name="TestingAgent",
            description="检验代理，负责出题和检验学习效果",
            model=utils.llm_model("qwen-plus"),
            tools=[generate_question_tool, evaluate_answer_tool],
            max_tool_iterations=3
        )
        
        return testing_agent
    
    def create_secretary_agent(self):
        """
        创建教秘Agent
        """
        # 创建制定学习计划工具
        create_study_plan_tool = utils.base_tool(
            tool_name="create_study_plan",
            tool_description="制定学习计划",
            parameters={
                "type": "object",
                "properties": {
                    "subject": {
                        "type": "string",
                        "description": "学科"
                    },
                    "topics": {
                        "type": "array",
                        "items": {"type": "string"},
                        "description": "要学习的知识点列表"
                    },
                    "days": {
                        "type": "integer",
                        "description": "计划天数"
                    }
                },
                "required": ["subject", "topics", "days"]
            }
        )
        
        def create_study_plan_func(subject: str, topics: list, days: int) -> str:
            plan = f"{subject}学习计划 ({days}天):\n"
            topics_per_day = max(1, len(topics) // days)
            for i in range(days):
                start_idx = i * topics_per_day
                end_idx = min((i + 1) * topics_per_day, len(topics))
                day_topics = topics[start_idx:end_idx]
                if day_topics:
                    plan += f"第{i+1}天: {', '.join(day_topics)}\n"
            return plan
        
        create_study_plan_tool.set_function(create_study_plan_func)
        
        # 创建教秘Agent
        secretary_agent = utils.base_agent(
            name="SecretaryAgent",
            description="教秘代理，负责整体教学计划和进度管理",
            model=utils.llm_model("qwen-plus"),
            tools=[create_study_plan_tool],
            max_tool_iterations=2
        )
        
        return secretary_agent
    
    def create_parent_agent(self):
        """
        创建家长Agent
        """
        # 创建生成报告工具
        generate_report_tool = utils.base_tool(
            tool_name="generate_report",
            tool_description="生成学习报告",
            parameters={
                "type": "object",
                "properties": {
                    "student_name": {
                        "type": "string",
                        "description": "学生姓名"
                    },
                    "subject": {
                        "type": "string",
                        "description": "学科"
                    },
                    "topics": {
                        "type": "array",
                        "items": {"type": "string"},
                        "description": "学习知识点列表"
                    },
                    "performance": {
                        "type": "string",
                        "description": "学习表现描述"
                    }
                },
                "required": ["student_name", "subject", "topics", "performance"]
            }
        )
        
        def generate_report_func(student_name: str, subject: str, topics: list, performance: str) -> str:
            report = f"""
学生 {student_name} 的 {subject} 学习报告:

学习内容:
- {', '.join(topics)}

学习表现:
{performance}

建议:
- 继续保持良好的学习习惯
- 针对薄弱环节加强练习
- 定期复习已学知识点
            """
            return report.strip()
        
        generate_report_tool.set_function(generate_report_func)
        
        # 创建家长Agent
        parent_agent = utils.base_agent(
            name="ParentAgent",
        description="家长代理，负责向家长报告学习情况",
            model=utils.llm_model("qwen-plus"),
            tools=[generate_report_tool],
            max_tool_iterations=2
        )
        
        return parent_agent
    
    def process_user_request(self, user_input: str) -> str:
        """
        处理用户请求，根据请求类型分发给相应的Agent
        
        Args:
            user_input (str): 用户输入
            
        Returns:
            str: 处理结果
        """
        # 简单的请求路由逻辑
        if any(keyword in user_input for keyword in ["学习", "教学", "讲解", "解释"]):
            agent = self.agents["teaching"]
            logger.info("将请求分发给教学Agent")
        elif any(keyword in user_input for keyword in ["测试", "练习", "题目", "考试"]):
            agent = self.agents["testing"]
            logger.info("将请求分发给检验Agent")
        elif any(keyword in user_input for keyword in ["计划", "安排", "进度"]):
            agent = self.agents["secretary"]
            logger.info("将请求分发给教秘Agent")
        elif any(keyword in user_input for keyword in ["报告", "家长", "情况"]):
            agent = self.agents["parent"]
            logger.info("将请求分发给家长Agent")
        else:
            # 默认使用教秘Agent
            agent = self.agents["secretary"]
            logger.info("默认将请求分发给教秘Agent")
            
        try:
            response = agent.run_once(user_input)
            return response
        except Exception as e:
            logger.error(f"处理用户请求时出错: {e}")
            return f"抱歉，在处理您的请求时出现了问题: {str(e)}"
    
    def run(self):
        """
        运行多Agent系统主循环
        """
        print("=" * 60)
        print("欢迎使用AI教师系统!")
        print("=" * 60)
        print("系统包含以下AI助手:")
        for name, agent in self.agents.items():
            print(f"- {agent.name}: {agent.description}")
        print("=" * 60)
        print("请输入您的学习需求，输入'退出'结束对话")
        print("=" * 60)
        
        while True:
            try:
                user_input = input("\n您: ").strip()
                
                if user_input.lower() in ['退出', 'quit', 'exit', 'bye']:
                    print("AI教师: 谢谢使用，再见!")
                    break
                
                if not user_input:
                    print("AI教师: 请输入有效内容")
                    continue
                
                # 处理用户请求
                response = self.process_user_request(user_input)
                print(f"AI教师: {response}")
                
            except KeyboardInterrupt:
                print("\n\nAI教师: 再见!")
                break
            except Exception as e:
                logger.error(f"运行时出错: {e}")
                print(f"AI教师: 发生未知错误: {str(e)}")


def main():
    """
    主函数
    """
    # 检查环境变量
    if not os.getenv("DASHSCOPE_API_KEY"):
        print("警告: 未设置DASHSCOPE_API_KEY环境变量，部分功能可能无法正常工作")
        print("请设置DASHSCOPE_API_KEY环境变量以获得完整功能")
    
    # 创建并运行多Agent系统
    multi_agent_system = MultiAgentSystem()
    multi_agent_system.run()


if __name__ == "__main__":
    main()