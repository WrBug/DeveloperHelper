package com.wrbug.developerhelper.ipc.processshare

import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

@Target(AnnotationTarget.FIELD,AnnotationTarget.FUNCTION)
@Retention(RetentionPolicy.RUNTIME)
annotation class DefaultValue(val value: String)
