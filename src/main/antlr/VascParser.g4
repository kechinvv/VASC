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
      NL* (EXTENDS NL* parentClass=identifier)?
      NL* IS NL* classBody NL* END
    ;

classBody
    : (memberDeclaration semi)*
    ;

memberDeclaration
    : variableDeclaration                                                                                  # InstanceVariableDeclaration
    | METHOD NL* identifier NL* parameters? NL* (COLON NL* returnType=className NL*)? IS NL* body NL* END  # MethodDeclaration
    | THIS NL* parameters? NL* IS NL* body NL* END                                                         # ConstructorDeclaration
    ;

variableDeclaration
    : VAR identifier COLON NL* className (NL* ASSIGN_OP NL* initExpression=expression)?
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
    : (statement semi)*
    ;

statement
    : identifier ASSIGN_OP NL* expression                                                               # AssignStatement
    | WHILE NL* condition=expression NL* LOOP NL* body NL* END                                          # WhileStatement
    | IF NL* condition=expression NL* THEN NL* thenBody=body NL* (ELSE NL* elseBody=body)? NL* END      # IfStatement
    | RETURN NL* expression?                                                                            # ReturnStatement
    | expression                                                                                        # ExpressionStatement
    | PRINT L_BRACKET STRING R_BRACKET                                                                  # PrintStatement
    | variableDeclaration                                                                               # VariableStatement
    ;

expression
    : THIS arguments? (NL* DOT call)*            # ThisExpression
    | SUPER arguments? (NL* DOT call)*           # SuperExpression
    | builtInType arguments? (NL* DOT call)*     # BuiltInExpression
    | call (NL* DOT call)*                       # CallableExpression
    | primary                                    # PrimaryExpression
    ;

call
    : identifier arguments?
    ;

arguments
    : L_BRACKET NL* (expression (NL* COMMA NL* expression)* NL*)? R_BRACKET
    ;

primary
    : MINUS? DIGIT+             # IntegerLiteral
    | MINUS? DIGIT+ DOT DIGIT+  # RealLiteral
    | TRUE                      # TrueLiteral
    | FALSE                     # FalseLiteral
    | NULL                      # NullLiteral
    ;

genericType
    : L_SQUARE_BRACKET NL* className NL* R_SQUARE_BRACKET
    ;

className
    : builtInType
    | identifier
    ;

builtInType
    : ARRAY genericType     # ArrayType
    | LIST genericType      # ListType
    | INT                   # IntegerType
    | BOOL                  # BooleanType
    | REAL                  # RealType
    ;

identifier
    : IDENTIFIER
    ;

semi
    : NL+
    ;