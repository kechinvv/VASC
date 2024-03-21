package com.vas;

import com.vas.gen.*;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.RuleNode;

import java.io.*;

public class Main {
    public static void main(String[] args) throws IOException {
        var example = new File("examples/FourBitAdder.vas");

        var stream = CharStreams.fromStream(new FileInputStream(example));
        var lexer = new VASCLexer(stream);
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
                    int endIndex = Math.min(node.getText().length(), 100);
                    System.out.println(rule + " ".repeat(30 - rule.length()) + node.getText().substring(0, endIndex));
                }
                return super.visitChildren(node);
            }
        };
        visitor.visit(tree);
    }
}