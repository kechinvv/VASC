import com.vas.antlr.VASCLexer
import com.vas.antlr.VASCParser
import org.antlr.v4.runtime.*
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.fail
import java.io.File

class TestValid {
    @TestFactory
    fun `test valid code`(): Collection<DynamicTest> {
        val dirPath = this::class.java.classLoader.resources("valid").iterator().next()
        val dir = File(dirPath.path)
        return dir.listFiles()!!.map { file ->
            DynamicTest.dynamicTest(file.nameWithoutExtension) {
                val lexer = VASCLexer(CharStreams.fromString(file.readText()))
                val parser = VASCParser(CommonTokenStream(lexer))
                val errorListener = object : BaseErrorListener() {
                    val errors = mutableListOf<String>()
                    override fun syntaxError(
                        recognizer: Recognizer<*, *>?,
                        offendingSymbol: Any?,
                        line: Int,
                        charPositionInLine: Int,
                        msg: String?,
                        e: RecognitionException?
                    ) {
                        val detailedMsg = """
                            |parser error at $line:$charPositionInLine
                            |$msg
                        """.trimMargin()
                        errors.add(detailedMsg)
                    }
                }
                parser.removeErrorListeners()
                parser.addErrorListener(errorListener)
                parser.program()
                if (errorListener.errors.isNotEmpty()) {
                    System.err.println(errorListener.errors.joinToString("\n\n"))
                    fail("unexpected parser errors in (${file.name}:1)")
                }
            }
        }
    }
}
