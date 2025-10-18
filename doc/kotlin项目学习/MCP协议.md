你的理解“方向”是对的，但术语上 MCP 官方文档故意把 **host** 和 **client** 的角色做了严格区分，而且 **client** 确实是 SDK 里一个具体类，而 **host** 并不是“你写的代码”，而是“**把 client 当积木拿去用的那一侧进程**”。下面用一句话先纠偏，再给一张“关系图”，最后把代码级细节拆给你看。

--------------------------------------------------
一句话纠偏
--------------------------------------------------
- **MCP Client** = SDK 提供的 `Client` 类（你直接 `new` 或注入即可）。  
- **MCP Host** = 真正“手握 LLM、决定何时调用工具”的那一侧进程（Claude Desktop、Cursor、Cline、Continue 等）；它内部会启动一个 **client 实例**去连接你的 server。  
- 我们自己的 Kotlin 程序如果想“反向”调用 server 的工具，也要**在自己的进程里 new 一个 client**，此时“你这段程序”就临时扮演了 host 角色。  

--------------------------------------------------
角色关系图（一次完整会话）
--------------------------------------------------
```
┌─────────────┐  stdio/SSE/WSS   ┌─────────────────┐
│ MCP Host    │ ◄--------------► │  MCP Server     │
│ (Claude/Cursor/   client       │ (你的 Kotlin)   │
│ Continue/你自己)               │  Server 实例    │
└─────────────┘                  └─────────────────┘
      ▲                                ▲
      │                                │
      ▼                                ▼
┌─────────────┐                  ┌─────────────┐
│  LLM 模型   │                  │ 本地工具/DB │
└─────────────┘                  └─────────────┘
```

--------------------------------------------------
代码级映射
--------------------------------------------------
1. **SDK 里只有三个核心类**  
   - `io.modelcontextprotocol.kotlin.sdk.Client` -> 负责“**我作为 host，去调用远端工具**”。  
   - `io.modelcontextprotocol.kotlin.sdk.Server` -> 负责“**我作为 server，暴露工具/资源/提示**”。  
   - `*Transport` -> 把上面俩角色按 JSON-RPC 2.0 塞到 stdio/SSE/WebSocket 通道里。  

2. **谁 new Client() 谁就是“临时 Host”**  
   示例：你写了一个批量单元测试，需要调用 MCP server 里的“清理数据库”工具——
   ```kotlin
   suspend fun main() {
       val client = Client(Implementation("test-host","1.0.0"))
       client.connect(StdioClientTransport(...))
       client.callTool("cleanDb", null)
       client.close()
   }
   ```
   这段程序里“你”就是 host，LLM 根本没出现，只是纯代码驱动。  

3. **Claude Desktop 的 host 进程长什么样？**  
   它是 Electron 主进程，内部用 TypeScript 同样 `new Client(...)`，再把用户消息发给 GPT/Claude 模型，模型如果返回 `tool_calls`，就调 `client.callTool()`——和你上面写的 Kotlin 代码逻辑完全一致，只是语言不同。  

--------------------------------------------------
小结（对照表）
--------------------------------------------------
| 术语   | 是否 SDK 类 | 谁提供        | 职责简述 |
|--------|-------------|---------------|----------|
| Host   | ❌（概念）  | Claude/Cursor/你自己 | 握LLM，决定何时调工具；内部会 `new Client` |
| Client | ✅（`Client`） | SDK 直接给出 | 替 host 发 JSON-RPC 请求，调工具/读资源 |
| Server | ✅（`Server`） | SDK 直接给出 | 暴露工具/资源/提示，等待 client 连接 |

所以：  
- **client** 确实是 SDK 里的一个类；  
- **host** 不是类，而是“**使用 client 去消费 MCP 能力**”的那一侧进程——可以是现成桌面软件，也可以是你临时写的一段 Kotlin 脚本。

下面给出一份「从零到生产」的完整实战手册，全部基于官方 **modelcontextprotocol/kotlin-sdk**（0.6.0+）。  
示例覆盖：环境准备 → 三种 transport（stdio / SSE / WebSocket）→ 工具/资源/提示三大核心能力 → 与 Claude/Cursor 实测互通 → 常见坑排查。  
你只需跟着步骤 copy 代码即可运行。

--------------------------------------------------
1. 版本与依赖
--------------------------------------------------
- 官方仓库：https://github.com/modelcontextprotocol/kotlin-sdk  
- Maven Central：io.modelcontextprotocol:kotlin-sdk:0.6.0  
- Ktor 引擎需自己追加（SDK 只暴露 API，不捆绑引擎）  

build.gradle.kts（Kotlin DSL）
```kotlin
plugins {
    kotlin("jvm") version "1.9.22"
    application
}
repositories { mavenCentral() }
dependencies {
    implementation("io.modelcontextprotocol:kotlin-sdk:0.6.0")

    /* 根据 transport 选引擎 */
    implementation("io.ktor:ktor-client-cio:2.3.11")        // client 用 CIO
    implementation("io.ktor:ktor-server-netty:2.3.11")       // server 用 Netty
    implementation("io.ktor:ktor-server-sse:2.3.11")         // SSE 需要
    implementation("io.ktor:ktor-websockets:2.3.11")         // WebSocket 需要

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
}
application {
    mainClass.set("com.example.MCPKt")
}
```

--------------------------------------------------
2. 最小可运行 Server（stdio）
--------------------------------------------------
特点：零端口、零网络，直接给 Claude Desktop / Cursor 当本地插件用。

src/main/kotlin/com/example/MCPServerStdio.kt
```kotlin
package com.example

import io.modelcontextprotocol.kotlin.sdk.*
import io.modelcontextprotocol.kotlin.sdk.server.*
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    val server = Server(
        serverInfo = Implementation(name = "demo-stdio", version = "1.0.0"),
        options = ServerOptions(
            capabilities = ServerCapabilities(
                tools = ServerCapabilities.Tools(listChanged = true)
            )
        )
    )

    /* 1. 注册工具 */
    server.addTool(
        name = "now",
        description = "获取服务器当前 UTC 时间"
    ) { _: JsonObject? ->
        CallToolResult(listOf(TextContent(Clock.System.now().toString())))
    }

    /* 2. 注册资源 */
    server.addResource(
        uri = "memory://info",
        name = "服务器内存信息",
        mimeType = "application/json"
    ) {
        val rt = Runtime.getRuntime()
        buildJsonObject {
            put("total", rt.totalMemory())
            put("free", rt.freeMemory())
        }.let { TextContent(it.toString()) }
    }

    /* 3. 注册提示模板 */
    server.addPrompt(
        name = "code-review",
        description = "生成代码审查提示词",
        arguments = listOf(PromptArgument("language", "编程语言", required = true))
    ) { args ->
        val lang = args["language"]?.jsonPrimitive?.content ?: "Kotlin"
        PromptResult(
            listOf(
                PromptMessage(
                    role = PromptMessage.Role.USER,
                    content = TextContent("请充当资深 $lang 工程师，对以下代码进行逐行评审...")
                )
            )
        )
    }

    /* 4. 启动 stdio 传输 */
    server.connect(StdioServerTransport())
    server.awaitClose()
}
```

运行
```bash
export DASHSCOPE_API_KEY=sk-xxx   # 如果工具里要调阿里云
./gradlew run
```

在 Claude Desktop 的 `claude_desktop_config.json` 里加：
```json
"mcpServers": {
  "demo-stdio": {
    "command": "/path/to/your/build/install/your-project/bin/your-project"
  }
}
```
重启 Claude，即可在输入框里直接 `@demo-stdio /now` 调用工具。

--------------------------------------------------
3. SSE 版 Server（浏览器/前端可直连）
--------------------------------------------------
适合：把 MCP 能力暴露给 Web 前端、低代码平台、Cursor 的「Remote MCP」。

src/main/kotlin/com/example/MCPServerSSE.kt
```kotlin
fun main() {
    embeddedServer(Netty, port = 3000) {
        install(SSE)
        routing {
            /* 与 stdio 示例同一套 Server 实例 */
            val server = createServer()   // 复用上面 server 构建逻辑
            mcpSseRoute(server)           // 官方扩展函数
        }
    }.start(wait = true)
}
```

Cursor 配置：
```
Host: http://localhost:3000/sse
```
无需任何插件，即可在 Cursor Chat 里调用 `now`、`memory://info`、`code-review`。

--------------------------------------------------
4. WebSocket 版 Server（双向低延迟）
--------------------------------------------------
只需把 `mcpSseRoute` 换成 `mcpWebSocketRoute`；客户端用 `WebSocketClientTransport`。

--------------------------------------------------
5. Client 侧调用（任意 JVM 程序都可当 Client）
--------------------------------------------------
src/main/kotlin/com/example/MCPClient.kt
```kotlin
suspend main() {
    val client = Client(
        clientInfo = Implementation(name = "demo-client", version = "1.0.0")
    )
    /* 1. 连接 stdio server */
    client.connect(StdioClientTransport(System.`in`, System.out))
    client.initialize()

    /* 2. 枚举工具 */
    val tools = client.listTools()
    println("可用工具=${tools.map { it.name }}")

    /* 3. 调用工具 */
    val result = client.callTool("now", null)
    println("返回=${result.content.first().text}")

    /* 4. 读取资源 */
    val resource = client.readResource("memory://info")
    println("资源内容=${resource.contents.first().text}")

    /* 5. 获取提示模板 */
    val prompt = client.getPrompt("code-review", mapOf("language" to JsonPrimitive("Java")))
    println("提示词=${prompt.messages.first().content.text}")

    client.close()
}
```

--------------------------------------------------
6. 与阿里云百炼（DashScope）联动
--------------------------------------------------
在工具实现里直接调用 DashScope 的「OpenAI-Compatible」接口即可：

```kotlin
server.addTool("sql2text") { args ->
    val sql = args?.get("sql")?.jsonPrimitive?.content ?: return@addTool
    val openai = OpenAI(
        token = System.getenv("DASHSCOPE_API_KEY"),
        baseUrl = "https://dashscope.aliyuncs.com/compatible-mode/v1"
    )
    val rsp = openai.chatCompletion(
        ChatCompletionRequest(
            model = "qwen3-coder-plus",
            messages = listOf(
                ChatMessage(
                    role = ChatMessage.Role.USER,
                    content = "把这条 SQL 翻译成自然语言：$sql"
                )
            )
        )
    )
    CallToolResult(listOf(TextContent(rsp.choices.first().message.content)))
}
```
Claude 提问「这句 SQL 干嘛的？」→ 自动触发 `sql2text` → 内部调用百炼 → 返回人类语言。

--------------------------------------------------
7. 调试 & 监控
--------------------------------------------------
- 日志：SDK 所有层都通过 `KtorClient` / `KtorServer` 暴露 `Logging` 插件，打开即可看到完整 JSON-RPC 报文。  
- 健康：Server 端可注册 `progress` 与 `cancel` 能力，客户端实时感知进度。  
- 热重载：stdio 进程被 Claude 拉起后，改代码→`./gradlew installDist`→重启 Claude 即可生效。

--------------------------------------------------
8. 常见坑
--------------------------------------------------
1. stdio 缓冲：在 Windows IDEA 里运行会卡住，务必用 `installDist` 生成脚本后再启动。  
2. 大对象返回：默认 64 KB，超过时在 `StdioServerTransport` 构造函数里调 `maxMessageSize = 256 * 1024`。  
3. SSE 跨域：前端调试时给 Ktor 加 `install(CORS) { anyHost() }`。  
4. 模型侧超时：DashScope 默认 60 s，流式场景记得 `.timeout(Duration.ofSeconds(120))`。

