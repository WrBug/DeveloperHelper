package com.wrbug.developerhelper.commonutil

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
    if (this == "0") {
        return true
    }
    val pattern = Pattern.compile("-?([0-9]+\\.0*)?[1-9][0-9]*$")
    return pattern.matcher(this).find()
}

fun String.isNumber(): Boolean {
    if (this == "0") {
        return true
    }
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

fun String?.toInt(): Int {
    this ?: return 0
    if (isInt()) {
        return BigInteger(this).toInt()
    }
    return 0
}

fun String?.toLong(): Long {
    this ?: return 0
    if (isNumber()) {
        return BigInteger(this).toLong()
    }
    return 0L
}

fun String?.toFloat(): Float {
    this ?: return 0F
    if (isDecimal()) {
        return BigDecimal(this).toFloat()
    }
    return 0F
}

fun String?.toDouble(): Double {
    this ?: return 0.0
    if (isDecimal()) {
        return BigDecimal(this).toDouble()
    }
    return 0.0
}