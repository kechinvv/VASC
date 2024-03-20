parser grammar VASCParser;

options { tokenVocab = VASCLexer; }

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

listType
    : LIST genericType
    ;

arrayType
    : ARRAY genericType
    ;

genericType
    : L_SQUARE_BRACKET NL* className NL* R_SQUARE_BRACKET
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

initializedVariable
    : VAR identifier COLON NL* expression
    ;

uninitializedVariable
    : VAR identifier COLON NL* type
    ;

parameters
    : L_BRACKET NL*
      (parameter (NL* COMMA NL* parameter)*)?
      NL* R_BRACKET
    ;

parameter
    : identifier  NL* COLON NL* type
    ;

body
    : (bodyStatement semi)*
    ;

bodyStatement
    : statement
    | variableDeclaration
    ;

methodDeclaration
    : METHOD NL* identifier NL* parameters? NL* (COLON NL* type NL*)? IS NL* body NL* END
    ;

constructorDeclaration
    : THIS NL* parameters? NL* IS NL* body NL* END
    ;

statement
    : assignment
    | whileLoop
    | ifStatement
    | returnStatement
    | expression
    | print
    ;

print
    : PRINT L_BRACKET STRING R_BRACKET
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

callableExpression
    : callable arguments? (NL* DOT callableExpression)?
    ;

arguments
    : L_BRACKET NL* (expression (NL* COMMA NL* expression)* NL*)? R_BRACKET
    ;

callable
    : THIS
    | SUPER
    | className
    ;

primary
    : integerLiteral
    | realLiteral
    | bool=(TRUE | FALSE)
    | NULL
    ;

type
    : className
    ;

className
    : identifier
    | arrayType
    | listType
    ;

integerLiteral
   :   MINUS? DIGIT+
   ;

realLiteral
   :   MINUS? DIGIT+ DOT DIGIT+
   ;

identifier
   : IDENTIFIER
   ;

semi
    : NL+
    ;