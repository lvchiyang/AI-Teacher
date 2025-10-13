import sys
import os
# 将项目根目录添加到Python路径中
sys.path.append(os.path.join(os.path.dirname(__file__), '..'))

import utils

"""千问api的tool参数结构:
tools_list = [
    {
        "type": "function",
        "function": {
            "name": "get_weather",
            "description": "获取指定城市的当前天气",
            "parameters": {
                "city": {
                    "type": "string",
                    "description": "城市名称，如'北京'、'上海'"
                },
            }
        }
    }
]
"""

"""千问模型调用工具时输出结构：
{
    "role": "assistant",
    "content": None,
    "toole_calls": [
        {
            "type": "function",
            "function": {
                "name": "get_weather",
                "arguments": {
                    "city": "北京"
                }
            }
        }
    ]
}
"""

# 使用(class)base_tool创建工具
tool = utils.base_tool("get_weather", "获取指定城市的当前天气", parameters = {"city":{"type":"string","description": "城市名称，如'北京'、'上海'"}})

# 使用类方法将工具类转化为llm可接受参数
tool_text = tool.to_tool_spec()
print(tool_text)

# 创建模型，在模型中尝试调用工具
qwen = utils.llm_model("qwen-plus", tools = [tool_text])
response = qwen.generate_text("今天北京的天气如何？")
print(response)

# 从json文件中创建工具
import json
tool_example = json.load(open("tool_example.json"))
print(type(tool_example))
tool_from_json = utils.base_tool.from_spec(tool_example)

# 向工具类中添加可执行方法
def func():
    print("hello world")
tool_from_json.set_function(func)
tool_from_json.tool_function()

# 向工具类中添加需要额外参数的可执行方法，只支持关键字参数
def func(name = None):
    print("hello world", name)
tool_from_json.set_function(func)
tool_from_json.tool_function(name = "xiaoming")


# 创建一个agent用于获得天气
get_weather = utils.base_tool.from_spec(json.load(open("get_weather.json")))
ask_human = utils.base_tool.from_spec(json.load(open("ask_human.json")))
finish = utils.base_tool.from_spec(json.load(open("finish.json")))
agent = utils.base_agent("assistant", model=utils.llm_model("qwen-plus"))

def get_weather_func(city: str) -> str:
    return f"The weather in {city} is sunny and warm."
def ask_human_func(question: str) -> str:
    print(question)
    return input("Human: ")
def finish_func(self) -> str:
    self.running = False

get_weather.set_function(get_weather_func)
ask_human.set_function(ask_human_func)
finish.set_function(finish_func)

agent.add_tool(get_weather)
agent.add_tool(ask_human)
agent.add_tool(finish)

agent.run_loop(["现在是什么天气?", "现在北京是什么天气?"])