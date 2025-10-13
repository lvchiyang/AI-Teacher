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
tool_from_json.set_function(func, accept_tool_input = False)
tool_from_json.tool_function(name = "xiaoming")