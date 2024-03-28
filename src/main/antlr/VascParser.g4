parser grammar VascParser;

options { tokenVocab = VascLexer; }

program
    :
      NL*
      (classDeclaration semi?)*
      EOF
    ;

classDeclaration
    : CLASS NL* identifier
      NL* (EXTENDS NL* identifier)?
      NL* IS NL* classBody NL* END
    ;

classBody
    : (memberDeclaration semi)*
    ;

memberDeclaration
    : variableDeclaration
    | methodDeclaration
    | constructorDeclaration
    ;

variableDeclaration
    : uninitializedVariable
    | initializedVariable
    ;

methodDeclaration
    : METHOD NL* identifier NL* parameters? NL* (COLON NL* className NL*)? IS NL* body NL* END
    ;

constructorDeclaration
    : THIS NL* parameters? NL* IS NL* body NL* END
    ;

initializedVariable
    : VAR identifier COLON NL* className NL* ASSIGN_OP NL* expression
    ;

uninitializedVariable
    : VAR identifier COLON NL* className
    ;

parameters
    : L_BRACKET NL*
      (parameter (NL* COMMA NL* parameter)*)?
      NL* R_BRACKET
    ;

parameter
    : identifier  NL* COLON NL* className
    ;

body
    : (bodyStatement semi)*
    ;

bodyStatement
    : statement
    | variableDeclaration
    ;

statement
    : assignment
    | whileLoop
    | ifStatement
    | returnStatement
    | expression
    | print
    ;

assignment
    : identifier ASSIGN_OP NL* expression
    ;

whileLoop
    : WHILE NL* expression NL* LOOP NL* body NL* END
    ;

ifStatement
    : IF NL* expression NL* THEN NL* body NL* (ELSE NL* body)? NL* END
    ;

returnStatement
    : RETURN NL* expression?
    ;

expression
    : callableExpression
    | primary
    ;

print
    : PRINT L_BRACKET STRING R_BRACKET
    ;

callableExpression
    : callable arguments? (NL* DOT callableExpression)?
    ;

callable
    : THIS
    | SUPER
    | builtInType
    | identifier
    ;

arguments
    : L_BRACKET NL* (expression (NL* COMMA NL* expression)* NL*)? R_BRACKET
    ;

primary
    : integerLiteral
    | realLiteral
    | boolLiteral
    | NULL
    ;

listType
    : LIST genericType
    ;

arrayType
    : ARRAY genericType
    ;

genericType
    : L_SQUARE_BRACKET NL* className NL* R_SQUARE_BRACKET
    ;

className
    : builtInType
    | identifier
    ;

builtInType
    : arrayType
    | listType
    | INT
    | BOOL
    | REAL
    ;

integerLiteral
    :   MINUS? DIGIT+
    ;

realLiteral
    :   MINUS? DIGIT+ DOT DIGIT+
    ;

boolLiteral
    : TRUE
    | FALSE
    ;
identifier
    : IDENTIFIER
    ;

semi
    : NL+
    ;