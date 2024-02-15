lexer grammar VASCLexer;

DOT : '.' ;

COLON : ':' ;

COMMA : ',' ;

L_BRACE : '{' ;

R_BRACE : '}' ;

L_BRACKET : '(' ;

R_BRACKET : ')' ;

L_SQUARE_BRACKET : '[' ;

R_SQUARE_BRACKET : ']' ;

EQ : '==' ;

L_ARROW : '<' ;

R_ARROW : '>' ;

L_ARROW_EQ : '<=' ;

R_ARROW_EQ : '>=' ;

ASSIGN_OP : ':=' ;

PERCENT : '%' ;

PLUS : '+' ;

MINUS : '-' ;

EXCLAMATION : '!' ;

ASTERISK : '*' ;

SLASH : '/' ;

INCREMENT : '++' ;

DECREMENT : '--' ;

PLUS_EQ : '+=' ;

MINUS_EQ : '-=' ;

EXCLAMATION_EQ : '!=' ;

ASTERISK_EQ : '*=' ;

SLASH_EQ : '/=' ;

PERCENT_EQ : '%=' ;

AMPERSAND : '&' ;

OR : '|' ;

XOR : '^' ;

TILDE : '~' ;

AMPERSAND_EQ : '&=' ;

OR_EQ : '|=' ;

XOR_EQ : '^=' ;

//DOUBLE_AMPERSAND : '&&' ;

//LOGIC_OR : '||' ;

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

END
    : 'end'
    ;

RETURN
    : 'return'
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

THIS
    : 'this'
    ;

IS
    : 'is'
    ;

TRUE
    : 'true'
    ;

FALSE
    : 'false'
    ;

IDENTIFIER
    :   [a-zA-Z_][a-zA-Z0-9_]*
    |   '`' .*? '`'
    ;

MlComment
    : '/*' ( MlComment | .)*? '*/' -> channel(HIDDEN)
    ;

COMMENT
    : ('//' ~[\r\n]*) -> channel(HIDDEN)
    ;

WS
    : [\u0020\u0009\u000C] -> channel(HIDDEN)
    ;

NL
    : '\n'
    | '\r' '\n'?
    ;

BAD_CHARACTER
   :   .
   ;