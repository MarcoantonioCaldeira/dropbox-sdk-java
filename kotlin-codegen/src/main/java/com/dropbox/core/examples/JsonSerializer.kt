package com.dropbox.core.examples

import kotlinx.serialization.json.Json

object JsonSerializer {
    val JSON = Json {
        prettyPrint = true
        prettyPrintIndent = " "
        isLenient = true
        ignoreUnknownKeys = true
        explicitNulls = false
    }
}

fun String.toIndentString(): String = buildString(length) {
    var indent = 0

    fun line() {
        appendLine()
        repeat(2 * indent) { append(' ') }
    }

    this@toIndentString.filter { it != ' ' }.forEach { char ->
        when (char) {
            ')', ']', '}' -> {
                indent--
                line()
                append(char)
            }
            '=' -> append(" = ")
            '(', '[', '{', ',' -> {
                append(char)
                if (char != ',') indent++
                line()
            }
            else -> append(char)
        }
    }
}