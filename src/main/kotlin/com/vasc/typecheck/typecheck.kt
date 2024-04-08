package com.vasc.typecheck

import com.vasc.VascTypeResolver
import com.vasc.antlr.VascParser.*
import com.vasc.antlr.VascParserBaseVisitor
import com.vasc.type.*

class TypeChecker(private val typeResolver: VascTypeResolver) : VascParserBaseVisitor<Unit>() {

}
