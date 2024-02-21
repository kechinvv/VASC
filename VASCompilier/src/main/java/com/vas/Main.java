package com.vas;

import com.vas.gen.VASCLexer;
import com.vas.gen.VASCParser;
import com.vas.gen.VASCParserBaseVisitor;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.RuleNode;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        var localPath = "examples/FourBitAdder.vas";

        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        var lexer = new VASCLexer(CharStreams.fromStream(classloader.getResourceAsStream(localPath)));
        var tokens = new CommonTokenStream(lexer);
        tokens.fill();
        for (Token token : tokens.getTokens()) {
            var tokenName = VASCLexer.VOCABULARY.getDisplayName(token.getType());
            System.out.println(tokenName + " ".repeat(15 - tokenName.length()) + token.getText());
        }

        var parser = new VASCParser(tokens);
        var tree = parser.program();
        var visitor = new VASCParserBaseVisitor<Void>() {

            @Override
            public Void visitChildren(RuleNode node) {
                var ruleId = node.getRuleContext().getRuleIndex();
                if (ruleId >= 0 && ruleId < VASCParser.ruleNames.length) {
                    var rule = VASCParser.ruleNames[ruleId];
                    int endIndex = Math.min(node.getText().length(), 50);
                    System.out.println(rule + " ".repeat(30 - rule.length()) + node.getText().substring(0, endIndex));
                }
                return super.visitChildren(node);
            }
        };
        visitor.visit(tree);
    }
}