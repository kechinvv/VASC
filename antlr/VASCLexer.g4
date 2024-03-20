lexer grammar VASCLexer;

DOT : '.' ;

COLON : ':' ;

COMMA : ',' ;

MINUS : '-' ;

L_BRACKET : '(' ;

R_BRACKET : ')' ;

L_SQUARE_BRACKET : '[' ;

R_SQUARE_BRACKET : ']' ;

ASSIGN_OP : ':=' ;

CLASS
    : 'class'
    ;

EXTENDS
    : 'extends'
    ;

METHOD
    : 'method'
    ;

WHILE
    : 'while'
    ;

LOOP
    : 'loop'
    ;

IS
    : 'is'
    ;

END
    : 'end'
    ;

RETURN
    : 'return'
    ;

LIST
    : 'List'
    ;

THIS
    : 'this'
    ;

ARRAY
    : 'Array'
    ;

PRINT
    : 'println'
    ;

SUPER
    : 'super'
    ;

IF
    : 'if'
    ;

THEN
    : 'then'
    ;

ELSE
    : 'else'
    ;

VAR
    : 'var'
    ;

TRUE
    : 'true'
    ;

FALSE
    : 'false'
    ;

NULL
    : 'null'
    ;

fragment ESCAPED_QUOTE
   : '\\"'
   ;

STRING
    : '"' ( ESCAPED_QUOTE | ~('\n'|'\r') )*? '"'
    ;

IDENTIFIER
    :   [a-zA-Z_$][a-zA-Z0-9_$]*
    ;

DIGIT: ('0'..'9');

NL
    : '\n'
    | '\r' '\n'?
    ;

MlComment
    : '/*' ( MlComment | .)*? '*/' -> channel(HIDDEN)
    ;

COMMENT
    : ('//' ~[\r\n]*) -> channel(HIDDEN)
    ;

WS
    : [ \t]+ -> channel(HIDDEN)
    ;


BAD_CHARACTER
   :   .
   ;