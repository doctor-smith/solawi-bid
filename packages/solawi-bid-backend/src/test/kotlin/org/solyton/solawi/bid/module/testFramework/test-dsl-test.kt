package org.solyton.solawi.bid.module.testFramework

import kotlinx.coroutines.runBlocking
import org.evoleq.test.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.solyton.solawi.bid.TestFramework
import java.util.stream.Stream
import kotlin.test.assertEquals

class TestDslTest {

    @TestFramework
    @ParameterizedTest
    @MethodSource("testCases")
    fun groupedTests(group: String, testCase: String) = runBlocking{
        test(group,testCase) {
            testGroup("group_1") {
                testCase("testcase_1"){
                    assertEquals(1,1)
                }
                testCase("testcase_2"){
                    assertEquals(1,1)
                }
                testCase("testcase_3"){
                    assertEquals(1,2)
                }
            }
            testGroup("group_2") {
                testCase("testcase_3"){
                    assertEquals(1,1)
                }
                testCase("testcase_4"){
                    assertEquals(1,1)
                }
            }
            testGroup("group_3") {
                testCase("testcase_5"){
                    assertEquals(1,1)
                }
                testCase("testcase_6"){
                    assertEquals(1,1)
                }
            }
        }
    }

    companion object {
        @JvmStatic
        fun testCases(): Stream<Arguments> = listOf(
            TestCases("group_1","testcase_1","testcase_2"),
            TestCases("group_2","testcase_3", "testcase_4"),
            TestCases("group_3","testcase_5", "testcase_6")
        ).flatten().map { Arguments.of(it.group, it.testCase) }.stream()
    }
}