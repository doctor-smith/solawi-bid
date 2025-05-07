package org.solyton.solawi.bid

import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.CLASS
import kotlin.annotation.AnnotationTarget.FUNCTION

@Target(CLASS, FUNCTION)
@Retention(RUNTIME)
@org.junit.jupiter.api.Tag("unit")
annotation class Unit

@Target(CLASS, FUNCTION)
@Retention(RUNTIME)
@org.junit.jupiter.api.Tag("api")
annotation class Api

@Target(CLASS, FUNCTION)
@Retention(RUNTIME)
@org.junit.jupiter.api.Tag("dbFunctional")
annotation class DbFunctional

@Target(CLASS, FUNCTION)
@Retention(RUNTIME)
@org.junit.jupiter.api.Tag("schema")
annotation class Schema

@Target(CLASS, FUNCTION)
@Retention(RUNTIME)
@org.junit.jupiter.api.Tag("testFramework")
annotation class TestFramework

