import sys
import os
import json

# 将项目根目录添加到Python路径中
sys.path.append(os.path.join(os.path.dirname(__file__), '..'))

import utils

def example_base_tool_usage():
    """
    演示 base_tool 类的基本用法
    """
    print("=== base_tool 使用示例 ===")
    
    # 方法1: 直接创建工具
    weather_tool = utils.base_tool(
        tool_name="get_weather",
        tool_description="获取指定城市的天气信息",
        parameters={
            "type": "object",
            "properties": {
                "city": {
                    "type": "string",
                    "description": "城市名称"
                },
                "date": {
                    "type": "string",
                    "description": "日期 (YYYY-MM-DD)"
                }
            },
            "required": ["city"]
        }
    )
    
    # 为工具设置功能函数
    def get_weather_impl(city: str, date: str = None) -> str:
        # 模拟天气查询结果
        if date:
            return f"{date} {city}的天气是晴天，温度25°C"
        else:
            return f"{city}今天的天气是晴天，温度25°C"
    
    weather_tool.set_function(get_weather_impl)
    
    # 转换为模型可用的工具规格
    tool_spec = weather_tool.to_tool_spec()
    print("工具规格:")
    print(json.dumps(tool_spec, indent=2, ensure_ascii=False))
    
    # 测试工具调用
    result = weather_tool.tool_function("北京")
    print(f"\n工具调用结果: {result}")
    
    # 方法2: 从规格创建工具
    tool_spec_dict = {
        "type": "function",
        "function": {
            "name": "calculate",
            "description": "执行数学计算",
            "parameters": {
                "type": "object",
                "properties": {
                    "expression": {
                        "type": "string",
                        "description": "数学表达式，如 '2+2'"
                    }
                },
                "required": ["expression"]
            }
        }
    }
    
    calculate_tool = utils.base_tool.from_spec(tool_spec_dict)
    
    def calculate_impl(expression: str) -> str:
        try:
            # 注意：实际应用中应该使用更安全的eval替代方案
            result = eval(expression)
            return f"{expression} = {result}"
        except Exception as e:
            return f"计算出错: {str(e)}"
    
    calculate_tool.set_function(calculate_impl)
    
    # 测试计算工具
    calculate_tool.set_function(calculate_impl)
    result = calculate_tool.tool_function("12*5")
    print(f"\n计算工具调用结果: {result}")

def example_llm_model_usage():
    """
    演示 llm_model 类的用法
    """
    print("\n=== llm_model 使用示例 ===")
    
    # 创建模型实例
    model = utils.llm_model(
        model_name="qwen-plus",
        temperature=0.7,
        max_tokens=512
    )
    
    # 创建一个简单的工具
    calculator_tool = utils.base_tool(
        tool_name="calculator",
        tool_description="执行简单数学计算",
        parameters={
            "type": "object",
            "properties": {
                "expression": {
                    "type": "string",
                    "description": "数学表达式"
                }
            },
            "required": ["expression"]
        }
    )
    
    def calc_func(expression: str) -> str:
        try:
            result = eval(expression)
            return str(result)
        except:
            return "计算错误"
    
    calculator_tool.set_function(calc_func)
    model.add_tool(calculator_tool)
    
    # 模拟对话历史
    messages = [
        {"role": "system", "content": "你是一个有用的助手，可以在需要时使用工具帮助用户"},
        {"role": "user", "content": "计算一下15乘以28等于多少？"}
    ]
    
    print("发送给模型的消息:")
    for msg in messages:
        print(f"  {msg['role']}: {msg['content']}")
    
    # 注：由于需要有效的API密钥才能真正调用模型，这里仅演示调用方式
    print("\n注意：实际调用需要有效的DASHSCOPE_API_KEY环境变量")

def example_context_memory_usage():
    """
    演示 ContextMemory 类的用法
    """
    print("\n=== ContextMemory 使用示例 ===")
    
    # 创建记忆实例
    memory = utils.ContextMemory(max_memory_size=10)
    
    # 添加记忆
    memory.add_memory({"role": "user", "content": "你好，我想了解一下天气情况"})
    memory.add_memory({"role": "assistant", "content": "您好！我可以帮您查询天气，请告诉我您想查询哪个城市的天气？"})
    memory.add_memory({"role": "user", "content": "我想知道北京的天气"})
    memory.add_memory({"role": "assistant", "content": "北京今天晴天，温度25°C"})
    
    # 获取记忆数量
    print(f"当前记忆条目数: {memory.get_memory_count()}")
    
    # 获取最近的记忆
    recent = memory.get_recent_memories(3)
    print("\n最近3条记忆:")
    for entry in recent:
        print(f"  ID: {entry.id}")
        print(f"  时间: {entry.timestamp}")
        print(f"  内容: {entry.content}")
        print()
    
    # 获取上下文（用于对话）
    context = memory.get_context(4)
    print("用于对话的上下文:")
    for item in context:
        print(f"  {item['role']}: {item['content']}")

def example_base_agent_usage():
    """
    演示 base_agent 类的用法
    """
    print("\n=== base_agent 使用示例 ===")
    
    # 创建工具
    def joke_tool_function(topic: str = "general") -> str:
        jokes = {
            "general": "为什么电脑去医院？因为它需要“重启”一下！",
            "animal": "为什么鸟儿不会打篮球？因为它们总是“飞”出界外！",
            "tech": "为什么程序员喜欢黑暗模式？因为他们不想被bug“亮”出来！"
        }
        return jokes.get(topic, jokes["general"])
    
    joke_tool = utils.base_tool(
        tool_name="tell_joke",
        tool_description="讲一个笑话",
        parameters={
            "type": "object",
            "properties": {
                "topic": {
                    "type": "string",
                    "description": "笑话主题 (general, animal, tech)",
                    "enum": ["general", "animal", "tech"]
                }
            }
        }
    )
    joke_tool.set_function(joke_tool_function)
    
    # 创建代理
    agent = utils.base_agent(
        name="ComedianBot",
        model=utils.llm_model("qwen-plus"),
        tools=[joke_tool],
        max_tool_iterations=2
    )
    
    # 显示代理信息
    print(f"代理名称: {agent.name}")
    print(f"工具数量: {len(agent.tools)}")
    print(f"最大工具迭代次数: {agent.max_tool_iterations}")
    
    # 查看代理的系统提示头
    print("\n代理系统提示:")
    print(agent.prompt_head["content"])

def main():
    """
    运行所有示例
    """
    print("Utils模块使用示例")
    print("=" * 50)
    
    example_base_tool_usage()
    example_llm_model_usage()
    example_context_memory_usage()
    example_base_agent_usage()
    
    print("\n" + "=" * 50)
    print("所有示例运行完毕")

if __name__ == "__main__":
    main()