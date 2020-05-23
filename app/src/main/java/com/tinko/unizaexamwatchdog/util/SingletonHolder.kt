package com.tinko.unizaexamwatchdog.util

/**
 * Parent class for all singletons which accept one argument.
 *
 * Inspiration: https://medium.com/@BladeCoder/kotlin-singletons-with-argument-194ef06edd9e.
 *
 * @param T singleton type.
 * @param A argument type.
 * @constructor
 * Constructs new singleton instance.
 *
 * @param creator constructor or initialization function.
 */
open class SingletonHolder<out T : Any, in A>(creator: (A) -> T) {
    private var creator: ((A) -> T)? = creator

    @Volatile
    private var instance: T? = null

    fun getInstance(arg: A): T {
        val i = instance
        if (i != null) {
            return i
        }

        return synchronized(this) {
            val i2 = instance
            if (i2 != null) {
                i2
            } else {
                val created = creator!!(arg)
                instance = created
                creator = null
                created
            }
        }
    }
}