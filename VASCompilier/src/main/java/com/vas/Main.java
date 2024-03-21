package com.vas;

import com.vas.gen.VASCLexer;
import com.vas.gen.VASCParser;
import com.vas.gen.VASCParserBaseVisitor;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.Interval;
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