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
    : variableDeclaration                                                                       # InstanceVariableDeclaration
    | METHOD NL* identifier NL* parameters? NL* (COLON NL* className NL*)? IS NL* body NL* END  # MethodDeclaration
    | THIS NL* parameters? NL* IS NL* body NL* END                                              # ConstructorDeclaration
    ;

variableDeclaration
    : VAR identifier COLON NL* className                                # UninitializedVariable
    | VAR identifier COLON NL* className NL* ASSIGN_OP NL* expression   # InitializedVariable
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
    : identifier ASSIGN_OP NL* expression                                   # AssignStatement
    | WHILE NL* expression NL* LOOP NL* body NL* END                        # WhileStatement
    | IF NL* expression NL* THEN NL* body NL* (ELSE NL* body)? NL* END      # IfStatement
    | RETURN NL* expression?                                                # ReturnStatement
    | expression                                                            # ExpressionStatement
    | PRINT L_BRACKET STRING R_BRACKET                                      # PrintStatement
    ;

expression
    : callable arguments? (NL* DOT CallableExpression)?     # CallableExpression
    | primary                                               # PrimaryExpression
    ;

callable
    : THIS          # ThisCallable
    | SUPER         # SuperCallable
    | builtInType   # BuiltInCallable
    | identifier    # IdentifierCallable
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