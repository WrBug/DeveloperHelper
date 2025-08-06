package com.wrbug.developerhelper.commonutil

object GlobalEvent {
    private val cache = hashMapOf<Action, HashMap<Any, () -> Unit>>()

    fun register(action: Action, bindKey: Any, callback: () -> Unit) {
        cache[action] = (cache[action] ?: HashMap()).apply {
            put(bindKey, callback)
        }
    }

    fun unRegister(action: Action, bindKey: Any) {
        cache[action]?.remove(bindKey)
    }

    fun postEvent(action: Action) {
        cache[action]?.values?.forEach {
            it()
        }
    }

    enum class Action {
        CloseAll
    }
}