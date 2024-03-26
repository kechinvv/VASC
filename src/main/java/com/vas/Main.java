package com.vas;

import com.vas.antlr.*;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.Interval;
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
                if (ruleId >= 0 && ruleId < VASCParser.ruleNames.length && node instanceof ParserRuleContext ctx) {
                    try {
                        String text;
                        if (ctx.start == null || ctx.stop == null || ctx.start.getStartIndex() < 0 || ctx.stop.getStopIndex() < 0)
                            text = ctx.getText();
                        else
                            text = ctx.start.getInputStream().getText(Interval.of(ctx.start.getStartIndex(), ctx.stop.getStopIndex()));
                        var rule = VASCParser.ruleNames[((ParserRuleContext) node).getRuleIndex()];
                        var lines = text.split("\n");
                        var maxIndent = 30;
                        lines[0] = ("|" + lines[0].indent(ctx.start.getCharPositionInLine())).indent(maxIndent - rule.length());
                        for (int i = 1; i < lines.length; i++) {
                            lines[i] = ("|" + lines[i]).indent(maxIndent);
                        }
                        System.out.println(rule + String.join("", lines));
                    } catch (Exception ignored) {
                    }
                }
                return super.visitChildren(node);
            }
        };
        visitor.visit(tree);
    }
}