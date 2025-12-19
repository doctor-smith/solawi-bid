package org.solyton.solawi.bid.module.testFramework

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.evoleq.math.x
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.solyton.solawi.bid.module.permission.action.db.isGranted
import org.solyton.solawi.bid.module.permission.schema.*
import org.solyton.solawi.bid.module.permission.schema.ContextEntity
import org.solyton.solawi.bid.module.permission.schema.RightEntity
import org.solyton.solawi.bid.module.permission.schema.RoleEntity
import org.solyton.solawi.bid.module.user.schema.UserEntity
import java.util.*


fun Routing.testContexts(database: Database) {
    get("/test/dummy-root-context") {
        val contextId =  transaction(database) {   ContextEntity.find { ContextsTable.name eq "DUMMY_ROOT_CONTEXT" }.first().id.value }
        call.respondText { contextId.toString() }
    }
    get("/test/auctions-application-context") {
        val contextId =  transaction(database) {   ContextEntity.find { ContextsTable.name eq "AUCTIONS" }.first().id.value }
        call.respondText { contextId.toString() }
    }
    get("/test/test-auction-context") {
        val contextId =  transaction(database) {   ContextEntity.find { ContextsTable.name eq "TEST_AUCTION_CONTEXT" }.first().id.value }
        call.respondText { contextId.toString() }
    }
    get("/test/context-by-name") {
        val contextName = call.request.queryParameters["context-name"] as String
        val contextId =  transaction(database) {
            val context = ContextEntity.find { ContextsTable.name eq contextName }.first()
            context.id.value.toString()
        }
        call.respondText(contextId)
    }
    get("/test/context-by-id") {
        val contextName = call.request.queryParameters["context-id"] as String
        val context =  transaction(database) {
            val context = ContextEntity.find { ContextsTable.id eq UUID.fromString(contextName) }.first()
            context
        }
        call.respondText(context.name)
    }
    get("/test/contexts-md"){
        val contexts =  transaction(database) {
            ContextEntity.all().orderBy(ContextsTable.name x SortOrder.ASC)
                .map { it.name x it.id.value }.distinct()
        }
        val md = """
            |
            |## Contexts
            |
            ||Name|Id|
            ||---|---|
            |${contexts.joinToString("\n") { (string, string1) -> "|$string|$string1|" }}
        """.trimMargin()
        call.respondText(md)
    }
    get("/test/role-id-by-name") {
        val roleName = call.request.queryParameters["role"] as String
        val role =  transaction(database) {
            val context = RoleEntity.find { RolesTable.name eq roleName }.first()
            context
        }
        call.respondText(role.id.value.toString())
    }
    get("/test/right-id-by-name") {
        val rightName = call.request.queryParameters["right"] as String
        val right =  transaction(database) {
            val context = RightEntity.find { RightsTable.name eq rightName }.first()
            context
        }
        call.respondText(right.id.value.toString())
    }
    get("/test/is-granted") {
        val userId = UUID.fromString(call.request.queryParameters["user-id"])
        val rightId = UUID.fromString(call.request.queryParameters["right-id"])
        val contextId = UUID.fromString(call.request.queryParameters["context-id"])
        try{
            val isGranted = transaction(database) {
                isGranted(userId, contextId, rightId)
            }
            call.respondText ( isGranted.toString() )
        } catch (ex: Exception) {
            call.respondText ( false.toString() )
        }
    }
    get("/test/users-right-role-contexts-md") {
        val userIdParam = call.request.queryParameters["user-id"] as String
        val userId = UUID.fromString(userIdParam)
        val username = transaction(database) {
            UserEntity.findById(userId)!!.username
        }
        val results: List<Triple<String,String,String>> = transaction(database) {
            UserRoleContext
                .join(RoleRightContexts, JoinType.INNER, UserRoleContext.contextId, RoleRightContexts.contextId)
                .join(RolesTable, JoinType.INNER, RoleRightContexts.roleId, RolesTable.id)
                .join(RightsTable, JoinType.INNER, RoleRightContexts.rightId, RightsTable.id)
                .join(ContextsTable, JoinType.INNER, RoleRightContexts.contextId, ContextsTable.id)
                .selectAll()
                .withDistinct()
                .where { UserRoleContext.userId eq userId }
                .orderBy(
                    ContextsTable.name x SortOrder.ASC,
                    RolesTable.name x SortOrder.ASC,
                    RightsTable.name x SortOrder.ASC
                ).distinct()
                .map { Triple(it[RolesTable.name], it[RightsTable.name], it[ContextsTable.name]) }
        }

        val mdTable = listOf<Triple<String,String,String>>(
            Triple("Role", "Right", "Context"),
            Triple("---","---","---"),
            *results.toTypedArray()
        ).joinToString("\n") { (string, string1, string2) ->
            "|$string|$string1|$string2|"
        }
        call.respondText (
            """
                |## Role Right Context of user $username
                |
                |$mdTable     
            """.trimMargin()

        )
    }

    get("/test/right-role-contexts-md-by-context-id") {
        val contextIdParam = call.request.queryParameters["context-id"] as String
        val contextId = UUID.fromString(contextIdParam)
        val contextName = transaction(database) { ContextEntity.findById(contextId)!!.name }
        val results: List<Triple<String,String,String>> = transaction(database) {
            RoleRightContexts
                //.join(RoleRightContexts, JoinType.INNER, UserRoleContext.contextId, RoleRightContexts.contextId)
                .join(RolesTable, JoinType.INNER, RoleRightContexts.roleId, RolesTable.id)
                .join(RightsTable, JoinType.INNER, RoleRightContexts.rightId, RightsTable.id)
                .join(ContextsTable, JoinType.INNER, RoleRightContexts.contextId, ContextsTable.id)
                .selectAll()
                .withDistinct()
                .where { RoleRightContexts.contextId eq contextId }
                .orderBy(
                    ContextsTable.name x SortOrder.ASC,
                    RolesTable.name x SortOrder.ASC,
                    RightsTable.name x SortOrder.ASC
                ).distinct()
                .map { Triple(it[RolesTable.name], it[RightsTable.name], it[ContextsTable.name]) }
        }

        val mdTable = listOf<Triple<String,String,String>>(
            Triple("Role", "Right", "Context"),
            Triple("---","---","---"),
            *results.toTypedArray()
        ).joinToString("\n") { (string, string1, string2) ->
            "|$string|$string1|$string2|"
        }
        call.respondText (
            """
                |## Role Right Context of context $contextName
                |
                |$mdTable     
            """.trimMargin()

        )
    }

}

suspend fun HttpClient.getDummyRootContextId(): String =  get("/test/context-by-name?context-name=DUMMY_ROOT_CONTEXT").bodyAsText()

suspend fun HttpClient.getTestContextIdByName(name: String): String = get("/test/context-by-name?context-name=$name").bodyAsText()

suspend fun HttpClient.getTestRoleIdByName(name: String): String = get("/test/role-id-by-name?role=$name").bodyAsText()

suspend fun HttpClient.getTestRightIdByName(name: String): String = get("/test/right-id-by-name?right=$name").bodyAsText()

suspend fun HttpClient.contextExists(id: UUID): Boolean =
    try {
        get("/test/context-by-id?context-id=$id")
        true
    } catch(_: Exception) {
        false
    }

suspend fun HttpClient.isGranted(userId: String, contextId: String, rightId: String): Boolean {
    val result = get("/test/is-granted?user-id=${userId}&context-id=$contextId&right-id=$rightId").bodyAsText()
    return when(result) {
        "true" -> true
        "false" -> false
        else -> false
    }
}

suspend fun HttpClient.getUsersRoleRightContextsAsMd(userId: String): String {
    val result = get("/test/users-right-role-contexts-md?user-id=$userId")

    return result.bodyAsText()
}

suspend fun HttpClient.getContextsAsMd(): String {
    val result = get("/test/contexts-md")
    return result.bodyAsText()
}

suspend fun HttpClient.getRoleRightContextsAsMdByContextId(contextId: String): String {
    val result = get("/test/right-role-contexts-md-by-context-id?context-id=$contextId")
    return result.bodyAsText()
}
