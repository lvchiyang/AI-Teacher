import utils
import json

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