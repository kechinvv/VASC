package com.vas.gen;// Generated from C:/Users/valer/IdeaProjects/VASC/antlr/VASCParser.g4 by ANTLR 4.13.1
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link VASCParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface VASCParserVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link VASCParser#program}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitProgram(VASCParser.ProgramContext ctx);
	/**
	 * Visit a parse tree produced by {@link VASCParser#classDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitClassDeclaration(VASCParser.ClassDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link VASCParser#identifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIdentifier(VASCParser.IdentifierContext ctx);
	/**
	 * Visit a parse tree produced by {@link VASCParser#type}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitType(VASCParser.TypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link VASCParser#memberDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMemberDeclaration(VASCParser.MemberDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link VASCParser#variableDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVariableDeclaration(VASCParser.VariableDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link VASCParser#methodDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMethodDeclaration(VASCParser.MethodDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link VASCParser#parameters}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParameters(VASCParser.ParametersContext ctx);
	/**
	 * Visit a parse tree produced by {@link VASCParser#parameterDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParameterDeclaration(VASCParser.ParameterDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link VASCParser#body}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBody(VASCParser.BodyContext ctx);
	/**
	 * Visit a parse tree produced by {@link VASCParser#bodyStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBodyStatement(VASCParser.BodyStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link VASCParser#constructorDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConstructorDeclaration(VASCParser.ConstructorDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link VASCParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStatement(VASCParser.StatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link VASCParser#assignment}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAssignment(VASCParser.AssignmentContext ctx);
	/**
	 * Visit a parse tree produced by {@link VASCParser#whileLoop}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWhileLoop(VASCParser.WhileLoopContext ctx);
	/**
	 * Visit a parse tree produced by {@link VASCParser#ifStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIfStatement(VASCParser.IfStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link VASCParser#returnStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReturnStatement(VASCParser.ReturnStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link VASCParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpression(VASCParser.ExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link VASCParser#arguments}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArguments(VASCParser.ArgumentsContext ctx);
	/**
	 * Visit a parse tree produced by {@link VASCParser#primary}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrimary(VASCParser.PrimaryContext ctx);
	/**
	 * Visit a parse tree produced by {@link VASCParser#integerLiteral}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIntegerLiteral(VASCParser.IntegerLiteralContext ctx);
	/**
	 * Visit a parse tree produced by {@link VASCParser#realLiteral}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRealLiteral(VASCParser.RealLiteralContext ctx);
}