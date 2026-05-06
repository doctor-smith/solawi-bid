package org.evoleq.language

import org.evoleq.language.get
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ParserTest {

    @Test
    fun variable() {

        val key = "name"
        val value = "Very long Text"
        val argument = "     $key   :  \"$value\"    "
        val expected = Lang.Variable(
            key,
            value
        )

        val parsed = Variable().run(argument)
        assertEquals(expected, parsed.result)
    }


    @Test
    fun simpleBlock() {
        val name = "name"
        val content1 = "key1: \"value1\""
        val content2 = "key2: \"value2\""
        val content3 = "key3: \"value3\""
        val arg = """
            |$name { 
            |   $content1 
            |   $content2
            |   block { $content3 }
            |   b{ x }
            |}
        """.trimMargin()

        val result = Block().run(arg)
      //  println(result)

      //  println(LanguageP().run(arg))
        assertTrue{result.result != null }
        //val value3 = result.result!!["name.block.key3"]
       // println(value3)
        //assertNotNull(value3 is Var)
      //  assertEquals("value3", value3)//.value)
    }


    @Test fun path() {
        val p = "x.y.z"

        val result = Path().run(p)

        assertEquals(listOf("x","y","z"), result.result)
    }

    @Test fun get() {
        val lang = Lang.Block(
            "x",
            listOf(
                Var("k1", "v1"),
                Var("k2", "v2"),
                Block("y", listOf(Var("c", "d"))),
                Block("z", listOf(Var("a", "b")))
            )
        )

        val v1 = lang["k1"]

        assertEquals("v1",v1)

        val b = lang["z.a"]
        assertEquals("b", b)
    }

    @Test fun component() {
        val c = Block("c", listOf())
        val lang = Lang.Block(
            "x",
            listOf(
                Var("k1", "v1"),
                Var("k2", "v2"),
                Block("y", listOf(c)),
                Block("z", listOf(Var("a", "b")))
            )
        )

        val result = lang.component("y.c")
        println(result)
        assertEquals(c, result)
    }

    @Test
    fun deeplyNestedStructure() {
        val lang = Lang.Block(
            "root",
            listOf(
                Var("rootKey", "rootValue"),
                Block(
                    "level1", listOf(
                        Var("key1", "value1"),
                        Block(
                            "level2", listOf(
                                Var("key2", "value2"),
                                Block(
                                    "level3", listOf(
                                        Var("key3", "value3"),
                                        Block(
                                            "level4", listOf(
                                                Var("key4", "value4"),
                                                Block(
                                                    "level5", listOf(
                                                        Var("key5", "value5"),
                                                        Block(
                                                            "level6", listOf(
                                                                Var("deepKey", "deepValue")
                                                            )
                                                        )
                                                    )
                                                )
                                            )
                                        )
                                    )
                                )
                            )
                        )
                    )
                ),
                Block(
                    "alternate", listOf(
                        Var("altKey", "altValue")
                    )
                )
            )
        )

        // Test root level
        assertEquals("rootValue", lang["rootKey"])

        // Test level 1
        assertEquals("value1", lang["level1.key1"])

        // Test level 2
        assertEquals("value2", lang["level1.level2.key2"])

        // Test level 3
        assertEquals("value3", lang["level1.level2.level3.key3"])

        // Test level 4
        assertEquals("value4", lang["level1.level2.level3.level4.key4"])

        // Test level 5
        assertEquals("value5", lang["level1.level2.level3.level4.level5.key5"])

        // Test level 6 (deepest)
        assertEquals("deepValue", lang["level1.level2.level3.level4.level5.level6.deepKey"])

        // Test alternate branch
        assertEquals("altValue", lang["alternate.altKey"])

        // Test component retrieval at various depths
        val level3Component = lang.component("level1.level2.level3")
        assertTrue { level3Component is Lang.Block }
        assertEquals("level3", (level3Component as Lang.Block).key)

        val level6Component = lang.component("level1.level2.level3.level4.level5.level6")
        assertTrue { level6Component is Lang.Block }
        assertEquals("level6", (level6Component as Lang.Block).key)
    }

    @Test
    fun tooMuchRecursion() {
        // Build an extremely deep nested structure programmatically
        fun buildDeepStructure(depth: Int): Lang.Block {
            var innerBlock: Lang.Block = Block("level$depth", listOf(Var("deepKey", "deepValue")))

            for (i in depth - 1 downTo 1) {
                innerBlock = Block("level$i", listOf(innerBlock))
            }

            return Block("root", listOf(innerBlock))
        }

        // Create a structure with 10000 levels to provoke stack overflow
        val veryDeepLang = buildDeepStructure(10000)

        // Build the path string
        val pathParts = (1..10000).map { "level$it" }
        val deepPath = pathParts.joinToString(".") + ".deepKey"

        // This would have caused stack overflow due to deep recursion earlier
        val result = veryDeepLang[deepPath]
        assertEquals("deepValue", result)
    }
}
