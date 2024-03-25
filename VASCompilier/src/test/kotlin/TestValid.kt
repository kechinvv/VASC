import com.vas.antlr.VASCLexer
import com.vas.antlr.VASCParser
import org.antlr.v4.runtime.*
import org.junit.jupiter.api.fail
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.io.File
import java.util.stream.Stream

class TestValid {
    companion object {
        @JvmStatic
        fun provideSources(): Stream<Arguments> {
            val dirPath = this::class.java.classLoader.resources("valid").iterator().next()
            val dir = File(dirPath.path)
            return dir.listFiles()!!.map {
                Arguments.of(object : File(it.path) {
                    override fun toString() = nameWithoutExtension
                })
            }.stream()
        }
    }

    @ParameterizedTest
    @MethodSource("provideSources")
    fun `test valid code`(file: File) {
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