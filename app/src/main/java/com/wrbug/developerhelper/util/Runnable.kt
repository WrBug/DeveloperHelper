package com.wrbug.developerhelper.util

import java.lang.Runnable

abstract class Runnable(vararg args: Any) : Runnable {
    protected var args: Array<out Any> = args

}