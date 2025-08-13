package org.solyton.solawi.bid.application.environment

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import org.evoleq.exposedx.data.Database
import org.evoleq.exposedx.data.DbEnv
import org.evoleq.ktorx.data.KTorEnv
import org.evoleq.ktorx.result.Result
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.solyton.solawi.bid.module.application.permission.Context
import org.solyton.solawi.bid.application.permission.Role
import org.solyton.solawi.bid.application.action.io.transform
import org.solyton.solawi.bid.module.authentication.environment.JWT
import org.solyton.solawi.bid.module.authentication.environment.JwtEnv
import org.solyton.solawi.bid.module.permission.schema.ContextEntity
import org.solyton.solawi.bid.module.permission.schema.Contexts
import org.solyton.solawi.bid.module.permission.schema.RoleEntity
import org.solyton.solawi.bid.module.permission.schema.Roles
import org.solyton.solawi.bid.module.user.schema.UserEntity
import org.solyton.solawi.bid.module.permission.schema.UserRoleContext
import org.solyton.solawi.bid.module.user.schema.Users
import java.util.UUID
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

    val mailService = MailService(
        Smtp(
            property("mail.smtp.host").getString(),
            property("mail.smtp.port").getString().toInt(),
            property("mail.smtp.auth").getString().toBoolean(),
            property("mail.smtp.user").getString(),
            property("mail.smtp.password").getString(),
            property("mail.smtp.startTslEnabled").getString().toBoolean()
        ),
        property("mail.defaultResponseAddress").getString()
    )

    Environment(
        database,
        jwt,
        applicationOwner,
        mailService
    )
}

data class Environment(
    val database: Database,
    override val jwt: JWT,
    val applicationOwner: User,
    val mailService: MailService,
    override val transformException: Result.Failure.Exception.() -> Pair<HttpStatusCode, Result.Failure.Message> = { transform() }
): KTorEnv, DbEnv, JwtEnv {
    lateinit var db: SqlDatabase

    override fun connectToDatabase(): SqlDatabase = when(::db.isInitialized){
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

            val appOwnerExists = UserEntity.find{ Users.username eq applicationOwner.username }.empty().not()
            if(appOwnerExists) return@transaction

            val applicationOwner = UserEntity.new {
                username = applicationOwner.username
                password = applicationOwner.password
                createdBy = UUID(0L,0L)
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
                it[userId] = applicationOwner.id.value
                it[contextId] = applicationContextId
                it[roleId] = ownerRoleId
            }

            UserRoleContext.insert {
                it[userId] = applicationOwner.id.value
                it[contextId] = applicationContextId
                it[roleId] = userRoleId
            }
        }
    }
}

data class User(
    val username: String,
    val password: String
)

data class MailService(
    val smtp: Smtp,
    val defaultResponseAddress: String?
)

data class Smtp(
    val host: String,
    val port: Int,
    val auth: Boolean,
    val user: String,
    val password: String,
    val startTslEnabled: Boolean
)
