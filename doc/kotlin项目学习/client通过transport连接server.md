情况 1：Server 通过 stdio 通信（推荐用于本地工具）
```kotlin
import io.modelcontextprotocol.kotlin.sdk.*
import io.modelcontextprotocol.kotlin.sdk.stdio.stdioTransport
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    // 创建 stdio transport（自动启动子进程）
    val transport = stdioTransport {
        command = "python"
        args = listOf("your_mcp_server.py") // 替换为你的 Server 启动命令
    }

    // 创建 MCP Client（Host 角色）
    val client = McpClient(transport)

    // 连接并初始化
    client.connect()
    client.initialize(
        InitializeRequest(
            protocolVersion = "2024-10-07",
            capabilities = ClientCapabilities()
        )
    )

    // 列出可用工具
    val tools = client.listTools(ListToolsRequest()).tools
    println("可用工具: ${tools.map { it.name }}")

    // 调用某个工具（例如 "get_weather"）
    val result = client.callTool(CallToolRequest(
        name = "get_weather",
        arguments = mapOf("location" to "Beijing")
    ))

    println("工具返回: $result")

    // 断开连接
    client.shutdown()
    client.disconnect()
}
```

```kotlin
command = "python"
args = listOf("your_mcp_server.py") // ← 这里到底填什么？
```

---

### ✅ 简短回答：

> **`"your_mcp_server.py"` 不只是“文件名”，而是你在终端中启动 MCP Server 时输入的完整命令中的“参数部分”。**

换句话说：**它应该和你在命令行里手动运行 Server 时敲的命令一致。**

---

### 🔍 举个实际例子

假设你有一个用 Python 写的 MCP Server，文件叫 `weather_server.py`，你平时在终端这样启动它：

```bash
python weather_server.py
```

那么在 Kotlin Host 代码中就应该写：

```kotlin
command = "python"
args = listOf("weather_server.py")
```

---

### 🧩 更复杂的例子

#### 情况 1：使用虚拟环境
你在终端运行：
```bash
/path/to/venv/bin/python weather_server.py --port 8080
```

那么 Kotlin 代码应为：
```kotlin
command = "/path/to/venv/bin/python"
args = listOf("weather_server.py", "--port", "8080")
```

#### 情况 2：用 Node.js 启动
终端命令：
```bash
node dist/server.js
```

Kotlin 代码：
```kotlin
command = "node"
args = listOf("dist/server.js")
```

#### 情况 3：用可执行文件（如 Go 编译后的二进制）
终端命令：
```bash
./my-mcp-server --config config.yaml
```

Kotlin 代码：
```kotlin
command = "./my-mcp-server"
args = listOf("--config", "config.yaml")
```

> ⚠️ 注意：如果 `command` 是相对路径（如 `./my-mcp-server`），要确保 Kotlin 进程的工作目录（working directory）和你手动运行时一致。

---

### 📁 文件路径说明

- `"weather_server.py"` 是**相对于 Kotlin Host 进程当前工作目录**的路径。
- 如果你的项目结构如下：
  ```
  my-kotlin-app/
  ├── build/
  ├── src/
  └── servers/weather_server.py   ← Server 文件在这里
  ```
  而你从 `my-kotlin-app/` 目录运行 Kotlin 程序，那么应该写：
  ```kotlin
  args = listOf("servers/weather_server.py")
  ```

或者使用绝对路径更可靠：
```kotlin
args = listOf("/full/path/to/servers/weather_server.py")
```

---

### 🛠 如何确认“启动命令”？

1. **打开终端**
2. **进入你的 MCP Server 项目目录**
3. **输入你平时启动 Server 的命令**（例如 `python server.py`）
4. **把这个命令拆成两部分**：
   - 第一个词（如 `python`、`node`、`./server`）→ `command`
   - 后面的所有词（如 `server.py`, `--debug`）→ `args` 列表

---

### ✅ 总结

| 你在终端输入的命令 | Kotlin 中的写法 |
|------------------|----------------|
| `python app.py` | `command = "python"`, `args = ["app.py"]` |
| `node index.js` | `command = "node"`, `args = ["index.js"]` |
| `./tool-server` | `command = "./tool-server"`, `args = []` |
| `python -m my_server --verbose` | `command = "python"`, `args = ["-m", "my_server", "--verbose"]` |

> 💡 **关键原则：Kotlin 的 `stdioTransport` 会 fork 一个子进程，执行 `command + args`，就像你在终端敲的一样。**

---

如果你告诉我：
- 你的 MCP Server 是用什么语言写的？
- 它的文件名是什么？
- 你平时怎么启动它？

我可以直接给你写出正确的 `command` 和 `args`！