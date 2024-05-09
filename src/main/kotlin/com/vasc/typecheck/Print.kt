package com.vasc.typecheck

import com.vasc.antlr.VascParser

sealed interface PrintMember

data class PrintVar(val id: String) : PrintMember
data class PrintString(val str: String) : PrintMember

fun VascParser.PrintStatementContext.members(): List<PrintMember> {
    val res = mutableListOf<PrintMember>()
    val text = STRING().text.removeSurrounding("\"")
    var isVar = false
    var buffer = ""
    for (ch in text) {
        if (!isVar) {
            if (ch == '$') {
                isVar = true
                res.add(PrintString(buffer))
                buffer = ""
            } else {
                buffer += ch
            }
        } else {
            if (ch != '_' && !ch.isDigit() && !ch.isLetter()) {
                isVar = false
                res.add(PrintVar(buffer))
                buffer = ""
            }
            buffer += ch
        }
    }
    if (isVar) {
        res.add(PrintVar(buffer))
    } else {
        res.add(PrintString(buffer))
    }
    return res
}
