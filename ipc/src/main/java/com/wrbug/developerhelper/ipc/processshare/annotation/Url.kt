package com.wrbug.developerhelper.ipc.processshare.annotation


@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Url(val value: String)
