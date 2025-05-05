package org.solyton.solawi.bid.application.environment

import io.ktor.server.application.*
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.solyton.solawi.bid.application.permission.Context
import org.solyton.solawi.bid.application.permission.Role
import org.solyton.solawi.bid.module.db.schema.*
import org.jetbrains.exposed.sql.Database as SqlDatabase

fun Application.setupEnvironment(): Environment = with(environment.config){
    val database = Database(
        url = property("database.url").getString(),
        driver = property("database.driver").getString(),
        user = property("database.user").getString(),
        password = property("database.password").getString()
    )

    val jwt = JWT(
        domain = property("jwt.domain").getString(),
        audience = property("jwt.audience").getString(),
        realm = property("jwt.realm").getString(),
        secret = property("jwt.secret").getString(),
    )

    val applicationOwner = User(
        username = property("users.owner.username").getString(),
        password = property("users.owner.password").getString()
    )

    Environment(
        database,
        jwt,
        applicationOwner
    )
}

data class Environment(
    val database: Database,
    val jwt: JWT,
    val applicationOwner: User
) {
    lateinit var db: SqlDatabase

    fun connectToDatabase(): SqlDatabase = when(::db.isInitialized){
        false-> SqlDatabase.connect(
            database.url,
            database.driver,
            database.user,
            database.password
        )
        true -> db
    }

    fun injectUsers(database: SqlDatabase) {
        transaction(database) {
            SchemaUtils.create(Users)

            val appOwnerExists = UserEntity.find{ Users.username eq applicationOwner.username }.firstOrNull() != null
            if(appOwnerExists) return@transaction

            val applicationOwner = UserEntity.new {
                username = applicationOwner.username
                password = applicationOwner.password
            }

            val applicationContextId = ContextEntity.find {
                Contexts.name eq Context.Application.value
            }.first().id.value

            val ownerRoleId = RoleEntity.find {
                Roles.name eq Role.owner.value
            }.first().id

            val userRoleId = RoleEntity.find{
                Roles.name eq Role.user.value
            }.first().id

            UserRoleContext.insert {
                it[userId] = applicationOwner.id
                it[contextId] = applicationContextId
                it[roleId] = ownerRoleId
            }

            UserRoleContext.insert {
                it[userId] = applicationOwner.id
                it[contextId] = applicationContextId
                it[roleId] = userRoleId
            }
        }
    }
}

data class JWT(
    val domain:String, // issuer
    val audience: String,
    val realm: String,
    val secret: String
)
data class Database(
    val url: String,
    val driver: String,
    val user: String,
    val password: String
)

data class User(
    val username: String,
    val password: String
)

