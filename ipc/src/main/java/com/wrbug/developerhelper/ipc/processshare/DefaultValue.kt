package com.wrbug.developerhelper.ipc.processshare

@Target(AnnotationTarget.FIELD,AnnotationTarget.FUNCTION)
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
annotation class DefaultValue(val value: String)
