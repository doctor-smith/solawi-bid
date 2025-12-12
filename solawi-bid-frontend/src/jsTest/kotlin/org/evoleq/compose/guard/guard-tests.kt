package org.evoleq.compose.guard

import androidx.compose.runtime.Composable
import kotlinx.browser.document
import org.evoleq.compose.guard.data.onEmpty
import org.evoleq.compose.guard.data.isLoading
import org.evoleq.compose.guard.data.onMissing
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.testutils.ComposeWebExperimentalTestsApi
import org.jetbrains.compose.web.testutils.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ComposeWebExperimentalTestsApi::class)
class GuardTests {

    @Test fun onMissingTest1() = runTest {
        val list = listOf<String>("a")
        val predicate: (String) -> Boolean = {it.isBlank()}
        var fired : Unit? = null
        composition {
            val missing = onMissing(
                {list},
                predicate,
                {fired = Unit}
            )
            assertTrue(missing)
        }
        assertNotNull(fired)
    }

    @Test fun onMissingTest2() = runTest {
        val list = listOf<String>("a", "b")
        val predicate: (String) -> Boolean = {it == "a"}
        var fired : Unit? = null
        composition {
            val missing = onMissing(
                {list},
                predicate,
                {fired = Unit}
            )
            assertTrue(!missing)
        }
        assertNull(fired)
    }

    @Test fun onEmptyTest1() = runTest {
        val list = listOf<String>()
        var fired: Unit? = null
        composition {
            val empty = onEmpty(
                {list},
                {fired = Unit}
            )
            assertTrue { empty}
        }
        assertNotNull(fired)
    }

    @Test fun onEmptyTest2() = runTest {
        val list = listOf<String>("a")
        var fired: Unit? = null
        composition {
            val empty = onEmpty(
                {list},
                {fired = Unit}
            )
            assertTrue { !empty }
        }
        assertNull(fired)
    }

    @Test fun isLoadingTest1() = runTest {
        val list1 = listOf<String>()
        val list2 = listOf<Int>()

        var fired1: Unit? = null
        var fired2: Unit? = null

        composition {
            val result = isLoading(
                onEmpty({list1}, {fired1 = Unit}),
                onEmpty({list2}, {fired2 = Unit}),
            )
            assertTrue { result }
        }
        assertEquals(Unit, fired1)
        assertEquals(Unit, fired2)
    }

    @Test fun isLoadingTest2() = runTest {
        val list1 = listOf<String>("")
        val list2 = listOf<Int>()

        var fired1: Unit? = null
        var fired2: Unit? = null

        composition {
            val result = isLoading(
                onEmpty({list1}, {fired1 = Unit}),
                onEmpty({list2}, {fired2 = Unit}),
            )
            assertTrue { result }
        }
        assertEquals(null, fired1)
        assertEquals(Unit, fired2)
    }

    @Test fun isLoadingTest3() = runTest {
        val list1 = listOf<String>()
        val list2 = listOf<Int>(1)

        var fired1: Unit? = null
        var fired2: Unit? = null

        composition {
            val result = isLoading(
                onEmpty({list1}, {fired1 = Unit}),
                onEmpty({list2}, {fired2 = Unit}),
            )
            assertTrue { result }
        }
        assertEquals(Unit, fired1)
        assertEquals(null, fired2)
    }

    @Test fun isLoadingTest4() = runTest {
        val list1 = listOf<String>("")
        val list2 = listOf<Int>(1)

        var fired1: Unit? = null
        var fired2: Unit? = null

        composition {
            val result = isLoading(
                onEmpty({list1}, {fired1 = Unit}),
                onEmpty({list2}, {fired2 = Unit}),
            )
            assertTrue { !result }
        }
        assertEquals(null, fired1)
        assertEquals(null, fired2)
    }

    @Test fun isLoadingImplTest1() = runTest {
        var isEmpty: Unit? = null
        @Composable fun x() {
            if(isLoading(onEmpty<Int>({emptyList<Int>()}){isEmpty = Unit})) return@x
            Div({id("id")}){}
        }
        composition {
            x()

        }
        assertEquals(Unit, isEmpty)
        assertNull(document.getElementById("id"))
    }


    @Test fun isnLoadingImplTest2() = runTest {
        var isEmpty: Unit? = null
        @Composable fun x() {
            if(isLoading(onEmpty<Int>({listOf<Int>(1)}){isEmpty = Unit})) return@x
            Div({id("id")}){}
        }
        composition {
            x()
        }
        assertEquals(null, isEmpty)
        assertNotNull(document.getElementById("id"))
    }
}
