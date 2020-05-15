package com.wrbug.developerhelper.ipcserver.annotation


/**
 *
 *  class: Controller.kt
 *  author: wrbug
 *  date: 2020-05-15
 *  description：
 *
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Controller(val value: String)