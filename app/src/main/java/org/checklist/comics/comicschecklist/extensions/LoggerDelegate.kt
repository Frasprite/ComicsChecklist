package org.checklist.comics.comicschecklist.extensions

import org.jetbrains.anko.AnkoLogger
import kotlin.reflect.KClass

import kotlin.reflect.full.companionObject

// Return logger for Java class, if companion object fix the name
fun <T : Any> logger(forClass: Class<T>): AnkoLogger {
    return AnkoLogger(unwrapCompanionClass(forClass).simpleName)
}

// Unwrap companion class to enclosing class given a Java Class
fun <T : Any> unwrapCompanionClass(ofClass: Class<T>): Class<*> {
    return ofClass.enclosingClass?.takeIf {
        ofClass.enclosingClass.kotlin.companionObject?.java == ofClass
    } ?: ofClass
}

// Unwrap companion class to enclosing class given a Kotlin Class
fun <T : Any> unwrapCompanionClass(ofClass: KClass<T>): KClass<*> {
    return unwrapCompanionClass(ofClass.java).kotlin
}

// Return logger for Kotlin class
fun <T : Any> logger(forClass: KClass<T>): AnkoLogger {
    return logger(forClass.java)
}

// Return logger from extended class (or the enclosing class)
fun <T : Any> T.logger(): AnkoLogger {
    return logger(this.javaClass)
}

// Return a lazy logger property delegate for enclosing class
fun <R : Any> R.lazyLogger(): Lazy<AnkoLogger> {
    return lazy { logger(this.javaClass) }
}

// Return a logger property delegate for enclosing class
fun <R : Any> R.injectLogger(): Lazy<AnkoLogger> {
    return lazyOf(logger(this.javaClass))
}

// Marker interface and related extension (remove extension for Any.logger() in favour of this)
interface Loggable

fun Loggable.logger(): AnkoLogger = logger(this.javaClass)

// Abstract base class to provide logging, intended for companion objects more than classes but works for either
abstract class WithLogging : Loggable {
    val LOG = logger()
}
