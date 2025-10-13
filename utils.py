from __future__ import annotations
from typing import Any, Dict, List, Optional
import json
import os
from typing import Optional, Dict, Any
from openai import OpenAI
from dataclasses import dataclass, field
from datetime import datetime


class base_agent:
    """
    智能体基础类：
    - 使用 llm_model 作为核心推理模型
    - 支持外围工具注册与调用（base_tool）
    - 简单运行周期：接收用户输入 -> 调用模型 -> 如模型要求调用工具则执行工具 -> 将工具结果反馈给模型 -> 返回最终响应
    """

    def __init__(
        self,
        name: str,
        model: "llm_model" = None,
        tools: Optional[List["base_tool"]] = None,
        memory: "ContextMemory" = None,
        max_tool_iterations: int = 3,
    ):
        self.name = name
        self.description: Optional[str] = None
        # 如果没有提供 model，可以创建一个默认占位模型实例（可在外部替换）
        self.model: "llm_model" = model or llm_model("qwen-turbo")
        self.tools: List["base_tool"] = tools or []
        self.memory: "ContextMemory" = memory or ContextMemory()
        self.max_tool_iterations = max_tool_iterations
        self.running: bool = False

    def add_tool(self, tool: "base_tool") -> None:
        """注册外围工具（工具应为 base_tool 的实例）"""
        if tool is None:
            return
        self.tools.append(tool)

    def get_tool(self, tool_name: str) -> Optional["base_tool"]:
        for t in self.tools:
            if getattr(t, "tool_name", None) == tool_name:
                return t
        return None

    def call_tool(self, tool_name: str, tool_input: Any = None, *args, **kwargs) -> Any:
        """
        调用已注册工具。
        工具应在 tool.tool_function 中实现实际逻辑。
        会把输入/输出写入工具实例的属性以便追踪。
        """
        tool = self.get_tool(tool_name)
        if not tool:
            raise ValueError(f"Tool not found: {tool_name}")
        if not callable(getattr(tool, "tool_function", None)):
            raise ValueError(f"Tool {tool_name} has no callable tool_function")
        # 保存输入
        tool.tool_input = tool_input
        # 执行工具
        result = tool.tool_function(tool_input, *args, **(kwargs or {}))
        tool.tool_output = result
        return result

    def _build_prompt(self, user_input: str) -> str:
        """根据记忆和工具信息构造提交给模型的 prompt"""
        ctx = self.memory.get_context(5)
        tools_info = [
            {"name": t.tool_name, "description": getattr(t, "tool_description", "")}
            for t in self.tools
        ]
        prompt_parts = [
            f"Agent: {self.name}",
            f"Description: {self.description or ''}",
            "Context:",
            json.dumps(ctx, ensure_ascii=False),
            "Available tools:",
            json.dumps(tools_info, ensure_ascii=False),
            "User input:",
            user_input,
            # 约定：如果模型想要调用工具，返回 JSON 结构如:
            # {"tool": "tool_name", "input": {...}}
            "If you want to call a tool, return a JSON object like {\"tool\": \"tool_name\", \"input\": ...}. Otherwise return a normal textual answer."
        ]
        return "\n\n".join(prompt_parts)

    def _parse_tool_call(self, model_output: str) -> Optional[Dict[str, Any]]:
        """
        尝试从模型输出解析工具调用请求。
        支持两种情况：
        - 直接 JSON 字符串： {"tool": "name", "input": ...}
        - 输出包含 JSON 片段：会尝试找到第一个 { ... } 并解析
        返回解析后的 dict 或 None（表示无需调用工具）
        """
        if not model_output.tool_calls:
            return None
        tool_calls_list = []
        for m in model_output.tool_calls:
            f = m.function
            # 解析 arguments 字符串为字典
            try:
                arguments_dict = json.loads(f.arguments)
            except (json.JSONDecodeError, TypeError) as e:
                print(f"Error parsing tool arguments: {e}")
                arguments_dict = f.arguments
            td = {"name": f.name, "arguments": arguments_dict}
            tool_calls_list.append(td)
        return tool_calls_list

    def run_once(self, user_input: str) -> str:
        """
        单次运行：
        - 构造 prompt（包含上下文）
        - 调用模型
        - 如果模型请求工具调用，执行工具并将结果反馈给模型，最多迭代 self.max_tool_iterations 次
        - 返回最终文本响应
        """
        # 记录用户输入到记忆
        self.memory.add_memory({"role": "user", "text": user_input})
        prompt = self._build_prompt(user_input)
        model_output = self.model.generate_text(prompt)
        if model_output is None:
            return "Error: model did not return a response."

        iterations = 0
        # 循环解析模型输出，看是否需要工具调用
        while iterations < self.max_tool_iterations:
            tool_call = self._parse_tool_call(model_output)
            if not tool_call:
                break
            tool_name = tool_call.get("tool")
            tool_input = tool_call.get("input")
            try:
                result = self.call_tool(tool_name, tool_input)
            except Exception as e:
                # 将错误反馈给模型并继续
                error_note = f"Tool {tool_name} call failed: {e}"
                followup_prompt = f"{model_output}\n\nToolResult: {json.dumps({'tool': tool_name, 'error': str(e)}, ensure_ascii=False)}\n\nPlease continue."
                model_output = self.model.generate_text(followup_prompt) or error_note
                iterations += 1
                continue

            # 把工具输出写入记忆并反馈给模型以便生成最终回答
            self.memory.add_memory({"role": "tool", "tool": tool_name, "output": result})
            followup_prompt = f"{model_output}\n\nToolResult: {json.dumps({'tool': tool_name, 'output': result}, ensure_ascii=False)}\n\nPlease continue and provide final answer."
            model_output = self.model.generate_text(followup_prompt) or str(result)
            iterations += 1

        # 将智能体最终回复写入记忆并返回
        self.memory.add_memory({"role": "agent", "text": model_output})
        return model_output

    def run_loop(self, input_iterable, stop_on_exception: bool = True):
        """
        简单运行循环：按照 input_iterable（可迭代的用户输入）逐条处理并产出响应
        """
        self.running = True
        outputs = []
        try:
            for user_input in input_iterable:
                if not self.running:
                    break
                try:
                    response = self.run_once(user_input)
                except Exception as e:
                    if stop_on_exception:
                        raise
                    response = f"Agent error: {e}"
                outputs.append(response)
            return outputs
        finally:
            self.running = False

    def stop(self) -> None:
        """停止运行循环（如果在 run_loop 中）"""
        self.running = False


class base_tool:
    def __init__(self, tool_name: str = None, tool_description: str = None, parameters: Dict[str, Any] = None, tool_type: str = "function"):
        """
        tool_name: 对应 JSON 中 function.name
        tool_description: 对应 JSON 中 function.description
        parameters: 对应 JSON 中 function.parameters（应为一个 dict 描述参数 schema）
        tool_type: 默认为 "function"（与 test.tools_list 格式一致）
        """
        self.tool_type: str = tool_type
        self.tool_name: str = tool.tool_name if False else tool_name  # 占位以保证格式一致
        # 替换为实际字段赋值
        self.tool_name: str = tool_name
        self.tool_description: str = tool_description
        self.parameters: Dict[str, Any] = parameters
        # tool_function 期望接受 (tool_input, *args, **kwargs) 以兼容 base_agent.call_tool
        self.tool_function: Optional[callable] = None
        self.tool_output: Any = None

    def set_function(self, func: callable):
        """
        设置工具的实际执行函数。
        - 如果 accept_tool_input=True，func 应该接受 (tool_input, *args, **kwargs)。
        - 如果 accept_tool_input=False，func 被认为接受 **kwargs（从 tool_input 字典展开）。
        """
        self.tool_function = func

    def init_function(self):
        """
        向后兼容：如果用户调用 init_function，希望 tool_function 自动调用某个方法，
        可以覆盖或调用 set_function 来设置实际实现。这里保持为 noop 或占位说明。
        """
        if self.tool_function is None:
            def _not_implemented(tool_input, *args, **kwargs):
                raise NotImplementedError("tool_function not set for tool: " + self.tool_name)
            self.tool_function = _not_implemented

    def to_tool_spec(self) -> Dict[str, Any]:
        """
        将 base_tool 转为与 test.tools_list 相同的字典结构：
        {"type": "function", "function": {"name": "...", "description": "...", "parameters": {...}}}
        """
        return {
            "type": self.tool_type,
            "function": {
                "name": self.tool_name,
                "description": self.tool_description,
                "parameters": self.parameters
            }
        }

    @classmethod
    def from_spec(cls, spec: Dict[str, Any], tool_function: Optional[callable] = None, accept_tool_input: bool = True) -> "base_tool":
        """
        从类似 test.tools_list 中的字典创建 base_tool 实例，可选地绑定实际执行函数。
        """
        func_block = spec.get("function", {})
        name = func_block.get("name", "")
        description = func_block.get("description", "")
        parameters = func_block.get("parameters", {})
        tool_type = spec.get("type", "function")
        t = cls(name, description, parameters, tool_type=tool_type)
        if tool_function is not None:
            t.set_function(tool_function, accept_tool_input=accept_tool_input)
        return t


class llm_model:
    def __init__(self, model_name: str, **kwargs):
        self.model_name: str = model_name
        self.temperature: float = kwargs.get("temperature", 0.7)
        self.top_k: int = kwargs.get("top_k", 50)
        self.top_p: float = kwargs.get("top_p", 1.0)
        self.response_format: dict = kwargs.get("response_format", {"type": "text"})
        self.max_tokens: int = kwargs.get("max_tokens", 1024)
        self.max_input_tokens: int = kwargs.get("max_input_tokens", 1024)
        self.enable_thinking: bool = kwargs.get("enable_thinking", False)
        self.tools: List[Any] = kwargs.get("tools", [])

    @classmethod
    def call_qwen_api(cls, input_text: str, **kwargs) -> Optional[str]:
        """
        调用Qwen API的类方法
        
        Args:
            input_text: 输入的文本字符串
            **kwargs: 其他可选参数，如api_key, model_name等
            
        Returns:
            Optional[str]: API返回的文本响应，失败时返回None
        """
        try:
            api_key = kwargs.get("api_key") or os.getenv("DASHSCOPE_API_KEY")
            if not api_key:
                raise ValueError("DASHSCOPE_API_KEY environment variable not set or api_key not provided")
            
            model_name = kwargs.get("model_name", "qwen-turbo")
            if not input_text or not isinstance(input_text, str):
                raise ValueError("Input text must be a non-empty string")
            
            client = OpenAI(
                api_key=api_key,
                base_url="https://dashscope.aliyuncs.com/compatible-mode/v1",
            )

            message = [
                {
                    "role": "system",
                    "content": "You are a helpful assistant."
                },
                {
                    "role": "user",
                    "content": input_text
                }
            ]
            
            completion = client.chat.completions.create(
                model=model_name,
                messages=message,
                stream=False,
                extra_body={"enable_thinking": kwargs.get("enable_thinking", False),
                            "top_k": kwargs.get("top_k", 50)},
                temperature=kwargs.get("temperature", 0.7),
                top_p=kwargs.get("top_p", 1.0),
                max_tokens=kwargs.get("max_tokens", 1024),
                tools=kwargs.get("tools", None)
            )
            if not hasattr(completion, "choices") or not completion.choices:
                return None
            re_message = completion.choices[0].message
            if not hasattr(message, "content"):
                pass
            return re_message
        except ValueError as e:
            # 记录参数错误
            print(f"ValueError in call_qwen_api: {e}")
            return None
        except Exception as e:
            # 记录其他错误
            print(f"Error calling Qwen API: {e}")
            return None

    def generate_text(self, input_text: str) -> Optional[str]:
        """
        使用实例配置调用Qwen API生成文本
        
        Args:
            prompt: 输入提示文本
            
        Returns:
            Optional[str]: 生成的文本，失败时返回None
        """
        return self.call_qwen_api(
            input_text,
            model_name=self.model_name,
            temperature=self.temperature,
            top_k=self.top_k,
            top_p=self.top_p,
            max_tokens=self.max_tokens,
            tools=self.tools,
            enable_thinking=self.enable_thinking
        )


@dataclass
class MemoryEntry:
    """表示单个记忆条目"""
    id: str
    content: Dict[str, Any]
    timestamp: datetime = field(default_factory=datetime.now)
    metadata: Dict[str, Any] = field(default_factory=dict)


class ContextMemory:
    """
    实现上下文记忆机制的类
    支持添加、检索和管理对话上下文记忆
    """

    def __init__(self, max_memory_size: int = 100):
        """
        初始化上下文记忆
        
        Args:
            max_memory_size: 最大记忆条目数量
        """
        self.max_memory_size = max_memory_size
        self.memories: List[MemoryEntry] = []
        self.memory_index: Dict[str, int] = {}  # id到索引的映射

    def add_memory(self, content: Dict[str, Any], memory_id: Optional[str] = None, 
                   metadata: Optional[Dict[str, Any]] = None) -> str:
        """
        添加新的记忆条目
        
        Args:
            content: 记忆内容
            memory_id: 记忆ID，如果未提供则自动生成
            metadata: 元数据
            
        Returns:
            str: 记忆ID
        """
        if memory_id is None:
            memory_id = f"memory_{len(self.memories)}"
            
        # 创建新的记忆条目
        entry = MemoryEntry(
            id=memory_id,
            content=content,
            timestamp=datetime.now(),
            metadata=metadata or {}
        )
        
        # 如果已达到最大记忆数，移除最旧的记忆
        if len(self.memories) >= self.max_memory_size:
            removed_entry = self.memories.pop(0)
            if removed_entry.id in self.memory_index:
                del self.memory_index[removed_entry.id]
        
        # 添加新记忆
        self.memories.append(entry)
        self.memory_index[memory_id] = len(self.memories) - 1
        
        return memory_id

    def get_memory(self, memory_id: str) -> Optional[MemoryEntry]:
        """
        根据ID获取特定记忆
        
        Args:
            memory_id: 记忆ID
            
        Returns:
            MemoryEntry: 记忆条目，如果未找到则返回None
        """
        index = self.memory_index.get(memory_id)
        if index is not None and 0 <= index < len(self.memories):
            entry = self.memories[index]
            if entry.id == memory_id:
                return entry
        return None

    def get_recent_memories(self, count: int = 5) -> List[MemoryEntry]:
        """
        获取最近的记忆条目
        
        Args:
            count: 要获取的记忆数量
            
        Returns:
            List[MemoryEntry]: 最近的记忆条目列表
        """
        return self.memories[-count:] if self.memories else []

    def search_memories(self, keyword: str) -> List[MemoryEntry]:
        """
        根据关键字搜索记忆
        
        Args:
            keyword: 搜索关键字
            
        Returns:
            List[MemoryEntry]: 匹配的记忆条目列表
        """
        results = []
        for entry in self.memories:
            # 在内容和元数据中搜索关键字
            content_str = json.dumps(entry.content, default=str)
            metadata_str = json.dumps(entry.metadata, default=str)
            
            if keyword.lower() in content_str.lower() or keyword.lower() in metadata_str.lower():
                results.append(entry)
                
        return results

    def update_memory(self, memory_id: str, content: Optional[Dict[str, Any]] = None,
                      metadata: Optional[Dict[str, Any]] = None) -> bool:
        """
        更新现有记忆
        
        Args:
            memory_id: 要更新的记忆ID
            content: 新的内容（可选）
            metadata: 新的元数据（可选）
            
        Returns:
            bool: 更新成功返回True，否则返回False
        """
        entry = self.get_memory(memory_id)
        if not entry:
            return False
            
        if content is not None:
            entry.content = content
            
        if metadata is not None:
            entry.metadata = metadata
            
        entry.timestamp = datetime.now()
        return True

    def delete_memory(self, memory_id: str) -> bool:
        """
        删除特定记忆
        
        Args:
            memory_id: 要删除的记忆ID
            
        Returns:
            bool: 删除成功返回True，否则返回False
        """
        index = self.memory_index.get(memory_id)
        if index is not None and 0 <= index < len(self.memories):
            entry = self.memories[index]
            if entry.id == memory_id:
                self.memories.pop(index)
                del self.memory_index[memory_id]
                # 更新索引
                self._rebuild_index()
                return True
        return False

    def _rebuild_index(self) -> None:
        """重建记忆索引"""
        self.memory_index = {entry.id: i for i, entry in enumerate(self.memories)}

    def get_all_memories(self) -> List[MemoryEntry]:
        """
        获取所有记忆条目
        
        Returns:
            List[MemoryEntry]: 所有记忆条目列表
        """
        return self.memories.copy()

    def clear_memories(self) -> None:
        """清空所有记忆"""
        self.memories.clear()
        self.memory_index.clear()

    def get_memory_count(self) -> int:
        """
        获取记忆条目数量
        
        Returns:
            int: 记忆条目数量
        """
        return len(self.memories)

    def get_context(self, count: int = 5) -> Dict[str, Any]:
        """
        获取上下文信息，用于对话系统
        
        Args:
            count: 包含的记忆条目数量
            
        Returns:
            Dict[str, Any]: 上下文信息
        """
        recent_memories = self.get_recent_memories(count)
        context = {
            "recent_memories": [
                {
                    "id": entry.id,
                    "content": entry.content,
                    "timestamp": entry.timestamp.isoformat(),
                    "metadata": entry.metadata
                }
                for entry in recent_memories
            ],
            "memory_count": len(self.memories),
            "timestamp": datetime.now().isoformat()
        }
        return context