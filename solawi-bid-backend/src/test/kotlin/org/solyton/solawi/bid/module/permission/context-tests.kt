package org.solyton.solawi.bid.module.permission

import org.evoleq.exposedx.test.runSimpleH2Test
import org.jetbrains.exposed.sql.selectAll
import org.solyton.solawi.bid.DbFunctional
import org.junit.jupiter.api.Test
import org.solyton.solawi.bid.module.db.repository.*
import org.solyton.solawi.bid.module.db.schema.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@Suppress("VariableNaming")
class ContextTests {

    val neededTables = arrayOf (
        Roles,
        Rights,
        Contexts,
        RoleRightContexts
    )

     @DbFunctional@Test fun getParentTest() = runSimpleH2Test(*neededTables){
         val root = ContextEntity.new {
             name = "Root"
         }

         val context1_1 = root.createChild("context1_1")
         val context1_2 = root.createChild("context1_2")
         val context1_1_1 = context1_1.createChild("context1_1_1")

         assertEquals(null, root.parent())
         assertEquals(root, context1_1.parent())
         assertEquals(root, context1_2.parent())
         assertEquals(context1_1, context1_1_1.parent())
     }

    @DbFunctional@Test fun computeContextPathTest() = runSimpleH2Test(*neededTables){
        val root = ContextEntity.new {
            name = "Root"
        }

        val context1_1 = root.createChild("context1_1")
        val context1_2 = root.createChild("context1_2")
        val context1_1_1 = context1_1.createChild("context1_1_1")

        assertEquals(
            listOf(root),
            root.pathAsList()
        )

        assertEquals(
            listOf(root, context1_1),
            context1_1.pathAsList()
        )

        assertEquals(
            listOf(root, context1_2),
            context1_2.pathAsList()
        )

        assertEquals(
            listOf(root,context1_1, context1_1_1),
            context1_1_1.pathAsList()
        )

        assertEquals(
            "${root.name}/${context1_1.name}",
            context1_1.path()
        )
        assertEquals(
            "${root.name}/${context1_2.name}",
            context1_2.path()
        )
        assertEquals(
            "${root.name}/${context1_1.name}/${context1_1_1.name}",
            context1_1_1.path()
        )
    }

    @DbFunctional@Test fun dropUniqueIndexOnName() = runSimpleH2Test(*neededTables) {

        exec("ALTER TABLE contexts DROP CONSTRAINT CONTEXTS_NAME_UNIQUE;")

        ContextEntity.new {
            name = "Context"
        }
        ContextEntity.new {
            name = "Context"
        }
        assertTrue { Contexts.selectAll().map { it }.size >= 2 }


    }

    @DbFunctional@Test fun nestContexts() = runSimpleH2Test(*neededTables) {
        ContextEntity.new {
            name = "ROOT"
        }
        ContextEntity.new {
            name = "ROOT/PARENT"
        }

        ContextEntity.new {
            name = "ROOT/PARENT/CHILD"
        }


        val contextsToMigrate = ContextEntity.find{Contexts.name like "%/%"}
        val root = ContextEntity.find { ContextsTable.name eq "ROOT" }.first()
        println(contextsToMigrate.map { it.name })

        contextsToMigrate.forEach {
            val names =it.name.split("/")

            // val contexts =
            ContextToNest(it, names, setOf(root)).nest()
        }
        val rootContext = ContextEntity.find{Contexts.name eq "ROOT"}
        assertEquals(1, rootContext.count())
        val parent = ContextEntity.find{Contexts.name eq "PARENT"}
        assertEquals(1, parent.count())

        val child = ContextEntity.find{Contexts.name eq "CHILD"}
        assertEquals(1, child.count())

        assertEquals(parent.first(), child.first().parent())
        assertEquals(rootContext.first(), parent.first().parent())
    }

    @DbFunctional@Test fun addContext() = runSimpleH2Test(*neededTables) {
        val root = ContextEntity.new {
            name = "ROOT"
        }

        val child = ContextEntity.new {
            name = "CHILD"
        }

        root.addChild(child)

        assertEquals(root, child.parent())
        assertEquals(0, root.left)
        assertEquals(3, root.right)
        assertEquals(1, child.left)
        assertEquals(2, child.right)

    }

    @DbFunctional@Test fun addContext2() = runSimpleH2Test(*neededTables) {
        val root = ContextEntity.new {
            name = "ROOT"
        }

        val child = ContextEntity.new {
            name = "CHILD"
        }

        root.addChild(child)

        assertEquals(root, child.parent())
        assertEquals(0, root.left)
        assertEquals(3, root.right)
        assertEquals(1, child.left)
        assertEquals(2, child.right)

        val root2 = ContextEntity.new {
            name = "ROOT2"
        }
        val child2 = ContextEntity.new {
            name = "CHILD2"
        }

        root2.addChild(child2)
        child2.addChild(root)

        assertEquals(root2, child2.parent())
        assertEquals(child2, root.parent())
        assertEquals(root, child.parent())


        assertEquals(0, root2.left)
        assertEquals(7, root2.right)
        assertEquals(1, child2.left)
        assertEquals(6, child2.right)

        assertEquals(2, root.left)
        assertEquals(5, root.right)
        assertEquals(3, child.left)
        assertEquals(4, child.right)

        assertEquals(0,root2.level)
        assertEquals(1,child2.level)
        assertEquals(2, root.level)
        assertEquals(3,child.level)
    }


    @DbFunctional@Test
    fun cloneContextTest() = runSimpleH2Test(*neededTables) {

        val context = createRootContext("c_root", )
        val child = context.createChild("child")

        val cloned = cloneContext(context.id.value, "_clone")

        assertEquals(2, cloned.size)
        cloned.forEach {
            assertTrue{it.name.endsWith("_clone")}
        }
    }
}
