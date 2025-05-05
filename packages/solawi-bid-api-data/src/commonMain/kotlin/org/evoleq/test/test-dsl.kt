package org.evoleq.test

import org.evoleq.math.MathDsl

@MathDsl
suspend fun String.testCase(name: String, test: suspend Any.()->Unit): Unit =
    if(name == this) test() else Unit

@MathDsl
suspend fun String.testGroup(group: (String)->Boolean, test: suspend Any.() -> Unit) =
    if(group(this)) test() else Unit

@MathDsl
suspend fun test(case: String, test: suspend String.() -> Unit): Unit = with(case){test()}

@MathDsl
suspend fun Any.setup(test: suspend Any.() -> Unit): Unit = test()

@MathDsl
suspend fun <T : Any> Any.parameters(test: suspend Any.() -> Array<T>): Array<T> = test()

data class TestCase(
    val group: String,
    val testCase: String,
    val skipped: Boolean = false
)

data class TestCases(
    val group: String,
    val testCases: List<String>
)

@MathDsl
suspend fun TestCase.testGroup(group: String, test: suspend Any.() -> Unit) =
    if(group == this.group) test() else Unit

@MathDsl
suspend fun test(group: String, case: String, test: suspend TestCase.() -> Unit): Unit = with(TestCase(group,case)){test()}

@MathDsl
suspend fun TestCase.testCase(case: String,skipped: Boolean = false, test: suspend Any.() -> Unit) =
    if(!skipped && case == testCase) test() else Unit

@MathDsl
fun List<TestCases>.flatten(): List<TestCase> = map {
    it.testCases.map {
        tC -> TestCase(it.group, tC)
    }
}.flatten()

@MathDsl
fun TestCases(group: String, vararg testCases: String) = TestCases(group, listOf(*testCases))

@MathDsl
fun flatten(vararg testCases: TestCases): List<TestCase> = testCases.toList().flatten()