parser grammar VASCParser;

options { tokenVocab = VASCLexer; }

program
    :
        classDeclaration+
        EOF
    ;

classDeclaration
    : CLASS className (EXTENDS className)? IS memberDeclaration* END
    ;

className
    :   IDENTIFIER (L_SQUARE_BRACKET className R_SQUARE_BRACKET)?
    ;

memberDeclaration
    :   variableDeclaration
    |   methodDeclaration
    |   constructorDeclaration
    ;

variableDeclaration
    :   VAR IDENTIFIER COLON expression
    ;

methodDeclaration
    :
    ;

parameters
    :
    ;

body
    :
    ;

parameterDeclaration
    :
    ;

constructorDeclaration
    :
    ;

statement
    :
    ;

assignment
    :
    ;

whileLoop
    :
    ;

ifStatement
    :
    ;

returnStatement
    : RETURN expression?
    ;

expression
    :
    ;

arguments
    :
    ;

primary
    : integerLiteral
    | realLiteral
    | THIS
    | className
    | bool=(TRUE | FALSE)
    ;

integerLiteral
   :   MINUS? DIGIT+
   |   DIGIT
   ;

realLiteral
   :   MINUS? DIGIT+ DOT DIGIT+
   ;
