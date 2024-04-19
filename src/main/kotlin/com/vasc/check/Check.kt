package com.vasc.check

import com.vasc.antlr.VascParser.ProgramContext

interface Check {
    fun check(program: ProgramContext)
}
