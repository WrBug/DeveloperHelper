package com.wrbug.developerhelper.commonutil
import java.lang.reflect.Field
import java.lang.reflect.Method

fun Class<*>.setValue(obj: Any?, fieldName: String, value: Any?) {
    this.matchField(fieldName)?.set(obj, value)
}

fun <T> Class<T>.getValue(obj: T?, fieldName: String): Any? {
    return this.matchField(fieldName)?.get(obj)
}

fun Any.getFieldValue(fieldName: String): Any? {
    return javaClass.getValue(this, fieldName)
}

fun Any.setFieldValue(fieldName: String, value: Any?) {
    javaClass.setValue(this, fieldName, value)
}

fun Any.callMethod(methodName: String, vararg args: Any?): Any? {
    return this.javaClass.matchMethod(methodName, *args)?.invoke(this, *args)
}

fun <T> Class<T>.callMethod(obj: T?, methodName: String, vararg args: Any?): Any? {
    return this.matchMethod(methodName, *args)?.invoke(obj, *args)
}

fun Class<*>.matchMethod(methodName: String, vararg args: Any?): Method? {
    return findDeclaredMethod(methodName, *args) ?: findMethod(methodName, *args)
}

fun Class<*>.matchField(fieldName: String): Field? {
    return findDeclaredField(fieldName) ?: findField(fieldName)
}

fun Class<*>.findDeclaredField(fieldName: String): Field? {
    return runCatching {
        this.getDeclaredField(fieldName).apply {
            if (!isAccessible) {
                isAccessible = true
            }
        }
    }.getOrElse {
        if (this == Any::class.java) {
            return null
        }
        this.superclass.findDeclaredField(fieldName)
    }
}

fun Class<*>.findDeclaredMethod(methodName: String, vararg args: Any?): Method? {
    val method =
        this.declaredMethods.asSequence()
            .filter { it.name == methodName && it.parameterCount == args.size }
            .find {
                val size = args.size
                for (i in 0 until size) {
                    val arg = args[i] ?: continue
                    if (!it.parameterTypes[i].isAssignableFrom(arg.javaClass)) {
                        return@find false
                    }
                }
                return@find true
            }?.apply {
                if (!isAccessible) {
                    isAccessible = true
                }
            }
    return method ?: if (this == Any::class.java) {
        null
    } else {
        superclass.findDeclaredMethod(methodName, *args)
    }
}

fun Class<*>.findField(fieldName: String): Field? {
    return runCatching {
        this.getField(fieldName).apply {
            if (!isAccessible) {
                isAccessible = true
            }
        }
    }.getOrElse {
        if (this == Any::class.java) {
            return null
        }
        this.superclass.findField(fieldName)
    }
}

fun Class<*>.findMethod(methodName: String, vararg args: Any?): Method? {
    val method =
        this.methods.asSequence()
            .filter { it.name == methodName && it.parameterCount == args.size }
            .find {
                val size = args.size
                for (i in 0..size) {
                    val arg = args[i] ?: continue
                    if (!it.parameterTypes[i].isAssignableFrom(arg.javaClass)) {
                        return@find false
                    }
                }
                return@find true
            }?.apply {
                if (!isAccessible) {
                    isAccessible = true
                }
            }
    return method ?: if (this == Any::class.java) {
        null
    } else {
        superclass.findDeclaredMethod(methodName, *args)
    }
}