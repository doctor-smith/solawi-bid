package org.solyton.solawi.bid

import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.CLASS
import kotlin.annotation.AnnotationTarget.FUNCTION

@Target(CLASS, FUNCTION)
@Retention(RUNTIME)
@org.junit.jupiter.api.Tag("regression")
annotation class Regression

@Target(CLASS, FUNCTION)
@Retention(RUNTIME)
@org.junit.jupiter.api.Tag("user-journey")
annotation class UserJourney

@Target(CLASS, FUNCTION)
@Retention(RUNTIME)
@org.junit.jupiter.api.Tag("workflow")
annotation class Workflow

@Target(CLASS, FUNCTION)
@Retention(RUNTIME)
@org.junit.jupiter.api.Tag("smoke")
annotation class Smoke

@Target(CLASS, FUNCTION)
@Retention(RUNTIME)
@org.junit.jupiter.api.Tag("security")
annotation class Security


@Target(CLASS, FUNCTION)
@Retention(RUNTIME)
@org.junit.jupiter.api.Tag("performance")
annotation class Performance

@Target(CLASS, FUNCTION)
@Retention(RUNTIME)
@org.junit.jupiter.api.Tag("develop")
annotation class Develop
