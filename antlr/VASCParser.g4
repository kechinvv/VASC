parser grammar VASCParser;

options { tokenVocab = VASCLexer; }

program
    :
      NL*
      classDeclaration*
      NL*
      EOF
    ;

classDeclaration
    : CLASS NL* identifier NL*
      (EXTENDS NL* identifier)? NL*
      IS NL* (memberDeclaration NL*)*  END NL*
    ;

identifier
    : IDENTIFIER type?
    ;

type
    : NL* L_SQUARE_BRACKET NL* IDENTIFIER type? R_SQUARE_BRACKET NL*
    ;

memberDeclaration
    : variableDeclaration
    | methodDeclaration
    | constructorDeclaration
    ;

variableDeclaration
    : VAR IDENTIFIER COLON NL* expression
    ;

methodDeclaration
    : METHOD IDENTIFIER NL* parameters? NL* (COLON IDENTIFIER)? NL* IS NL* body NL* END
    ;

parameters
    : L_BRACKET NL*
      parameterDeclaration NL*
      (NL* COMMA NL* parameterDeclaration NL*)*
      R_BRACKET
    ;

parameterDeclaration
    : IDENTIFIER COLON NL* identifier
    ;

body
    : (bodyStatement NL*)*
    ;

bodyStatement
    : statement
    | variableDeclaration
    ;

constructorDeclaration
    : THIS NL* parameters? NL* IS NL* body NL* END
    ;

statement
    : assignment
    | whileLoop
    | ifStatement
    | returnStatement
    ;

assignment
    : IDENTIFIER ASSIGN_OP NL* expression
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
    : primary (NL* DOT IDENTIFIER NL* arguments?)*
    ;

arguments
    : L_BRACKET NL* expression (NL* COMMA NL* expression)* NL* R_BRACKET
    ;

primary
    : integerLiteral
    | realLiteral
    | THIS
    | identifier
    | bool=(TRUE | FALSE)
    ;

integerLiteral
   :   MINUS? DIGIT+
   ;

realLiteral
   :   MINUS? DIGIT+ DOT DIGIT+
   ;
