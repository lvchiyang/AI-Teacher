import io.github.classgraph.ClassGraph
import kotlin.reflect.KClass
import kotlin.reflect.full.*
import java.io.File

fun main() {
    File("api.txt").bufferedWriter().use { out ->
        ClassGraph()
            .acceptPackages("io.modelcontextprotocol")   // MCP SDK的包名前缀
            .enableAllInfo()
            .scan().use { scan ->
                scan.allClasses
                    .filter { it.isPublic }
                    .map { it.loadClass().kotlin }
                    .forEach { kcls -> dump(kcls, out) }
            }
    }
    println("✅ 完成，共输出 ${File("api.txt").readLines().size} 行")
}

private fun dump(kcls: KClass<*>, out: java.io.BufferedWriter) {
    out.appendLine("### ${kcls.qualifiedName}")
    // 1. 构造函数
    kcls.constructors.forEach { out.appendLine("  ctor  $it") }
    // 2. 静态/伴生函数
    kcls.staticFunctions.forEach { out.appendLine("  static $it") }
    // 3. 成员函数（含扩展）
    kcls.declaredFunctions.forEach { out.appendLine("  fun    $it") }
    // 4. 公开属性
    kcls.declaredMemberProperties.forEach { out.appendLine("  val/var $it") }
    out.appendLine("")
}
