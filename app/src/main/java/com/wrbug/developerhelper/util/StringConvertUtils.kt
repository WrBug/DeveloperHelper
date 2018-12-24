package com.wrbug.developerhelper.util

import java.math.BigDecimal
import java.math.BigInteger
import java.util.regex.Pattern


fun String.isInt(): Boolean {
    if (isNumber()) {
        return BigInteger(this).toLong() < Integer.MAX_VALUE && BigInteger(this).toLong() > Integer.MIN_VALUE
    }
    return false
}

fun String.isDecimal(): Boolean {
    val pattern = Pattern.compile("-?([0-9]+\\.)?[1-9][0-9]*$")
    return pattern.matcher(this).find()
}
fun String.isNumber(): Boolean {
    val pattern = Pattern.compile("-?[1-9][0-9]*$")
    return pattern.matcher(this).find()
}
fun String.isBoolean(): Boolean {
    return this.toLowerCase() == "true" || this.toLowerCase() == "false"
}

fun String.toBoolean(): Boolean {
    if (isBoolean()) {
        return this.toLowerCase() == "true"
    }
    return false
}

fun String.toInt(): Int {
    if (isInt()) {
        return BigInteger(this).toInt()
    }
    return 0
}


fun String.toLong(): Long {
    if (isNumber()) {
        return BigInteger(this).toLong()
    }
    return 0L
}

