package com.vas.gen;// Generated from C:/Users/valer/IdeaProjects/VASC/antlr/VASCParser.g4 by ANTLR 4.13.1
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link VASCParser}.
 */
public interface VASCParserListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link VASCParser#program}.
	 * @param ctx the parse tree
	 */
	void enterProgram(VASCParser.ProgramContext ctx);
	/**
	 * Exit a parse tree produced by {@link VASCParser#program}.
	 * @param ctx the parse tree
	 */
	void exitProgram(VASCParser.ProgramContext ctx);
	/**
	 * Enter a parse tree produced by {@link VASCParser#classDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterClassDeclaration(VASCParser.ClassDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link VASCParser#classDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitClassDeclaration(VASCParser.ClassDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link VASCParser#identifier}.
	 * @param ctx the parse tree
	 */
	void enterIdentifier(VASCParser.IdentifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link VASCParser#identifier}.
	 * @param ctx the parse tree
	 */
	void exitIdentifier(VASCParser.IdentifierContext ctx);
	/**
	 * Enter a parse tree produced by {@link VASCParser#type}.
	 * @param ctx the parse tree
	 */
	void enterType(VASCParser.TypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link VASCParser#type}.
	 * @param ctx the parse tree
	 */
	void exitType(VASCParser.TypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link VASCParser#memberDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterMemberDeclaration(VASCParser.MemberDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link VASCParser#memberDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitMemberDeclaration(VASCParser.MemberDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link VASCParser#variableDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterVariableDeclaration(VASCParser.VariableDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link VASCParser#variableDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitVariableDeclaration(VASCParser.VariableDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link VASCParser#methodDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterMethodDeclaration(VASCParser.MethodDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link VASCParser#methodDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitMethodDeclaration(VASCParser.MethodDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link VASCParser#parameters}.
	 * @param ctx the parse tree
	 */
	void enterParameters(VASCParser.ParametersContext ctx);
	/**
	 * Exit a parse tree produced by {@link VASCParser#parameters}.
	 * @param ctx the parse tree
	 */
	void exitParameters(VASCParser.ParametersContext ctx);
	/**
	 * Enter a parse tree produced by {@link VASCParser#parameterDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterParameterDeclaration(VASCParser.ParameterDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link VASCParser#parameterDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitParameterDeclaration(VASCParser.ParameterDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link VASCParser#body}.
	 * @param ctx the parse tree
	 */
	void enterBody(VASCParser.BodyContext ctx);
	/**
	 * Exit a parse tree produced by {@link VASCParser#body}.
	 * @param ctx the parse tree
	 */
	void exitBody(VASCParser.BodyContext ctx);
	/**
	 * Enter a parse tree produced by {@link VASCParser#bodyStatement}.
	 * @param ctx the parse tree
	 */
	void enterBodyStatement(VASCParser.BodyStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link VASCParser#bodyStatement}.
	 * @param ctx the parse tree
	 */
	void exitBodyStatement(VASCParser.BodyStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link VASCParser#constructorDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterConstructorDeclaration(VASCParser.ConstructorDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link VASCParser#constructorDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitConstructorDeclaration(VASCParser.ConstructorDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link VASCParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterStatement(VASCParser.StatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link VASCParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitStatement(VASCParser.StatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link VASCParser#assignment}.
	 * @param ctx the parse tree
	 */
	void enterAssignment(VASCParser.AssignmentContext ctx);
	/**
	 * Exit a parse tree produced by {@link VASCParser#assignment}.
	 * @param ctx the parse tree
	 */
	void exitAssignment(VASCParser.AssignmentContext ctx);
	/**
	 * Enter a parse tree produced by {@link VASCParser#whileLoop}.
	 * @param ctx the parse tree
	 */
	void enterWhileLoop(VASCParser.WhileLoopContext ctx);
	/**
	 * Exit a parse tree produced by {@link VASCParser#whileLoop}.
	 * @param ctx the parse tree
	 */
	void exitWhileLoop(VASCParser.WhileLoopContext ctx);
	/**
	 * Enter a parse tree produced by {@link VASCParser#ifStatement}.
	 * @param ctx the parse tree
	 */
	void enterIfStatement(VASCParser.IfStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link VASCParser#ifStatement}.
	 * @param ctx the parse tree
	 */
	void exitIfStatement(VASCParser.IfStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link VASCParser#returnStatement}.
	 * @param ctx the parse tree
	 */
	void enterReturnStatement(VASCParser.ReturnStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link VASCParser#returnStatement}.
	 * @param ctx the parse tree
	 */
	void exitReturnStatement(VASCParser.ReturnStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link VASCParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterExpression(VASCParser.ExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link VASCParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitExpression(VASCParser.ExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link VASCParser#arguments}.
	 * @param ctx the parse tree
	 */
	void enterArguments(VASCParser.ArgumentsContext ctx);
	/**
	 * Exit a parse tree produced by {@link VASCParser#arguments}.
	 * @param ctx the parse tree
	 */
	void exitArguments(VASCParser.ArgumentsContext ctx);
	/**
	 * Enter a parse tree produced by {@link VASCParser#primary}.
	 * @param ctx the parse tree
	 */
	void enterPrimary(VASCParser.PrimaryContext ctx);
	/**
	 * Exit a parse tree produced by {@link VASCParser#primary}.
	 * @param ctx the parse tree
	 */
	void exitPrimary(VASCParser.PrimaryContext ctx);
	/**
	 * Enter a parse tree produced by {@link VASCParser#integerLiteral}.
	 * @param ctx the parse tree
	 */
	void enterIntegerLiteral(VASCParser.IntegerLiteralContext ctx);
	/**
	 * Exit a parse tree produced by {@link VASCParser#integerLiteral}.
	 * @param ctx the parse tree
	 */
	void exitIntegerLiteral(VASCParser.IntegerLiteralContext ctx);
	/**
	 * Enter a parse tree produced by {@link VASCParser#realLiteral}.
	 * @param ctx the parse tree
	 */
	void enterRealLiteral(VASCParser.RealLiteralContext ctx);
	/**
	 * Exit a parse tree produced by {@link VASCParser#realLiteral}.
	 * @param ctx the parse tree
	 */
	void exitRealLiteral(VASCParser.RealLiteralContext ctx);
}