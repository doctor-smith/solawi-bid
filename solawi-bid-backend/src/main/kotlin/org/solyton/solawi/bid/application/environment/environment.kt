package org.solyton.solawi.bid.application.environment

import io.ktor.http.*
import io.ktor.server.application.*
import org.evoleq.exposedx.data.Database
import org.evoleq.exposedx.data.DbEnv
import org.evoleq.ktorx.data.KTorEnv
import org.evoleq.ktorx.result.Result
import org.evoleq.uuid.UUID_ZERO
import org.jetbrains.exposed.sql.DatabaseConfig
import org.jetbrains.exposed.sql.ExperimentalKeywordApi
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Slf4jSqlDebugLogger
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.solyton.solawi.bid.application.action.io.transform
import org.solyton.solawi.bid.application.permission.Role
import org.solyton.solawi.bid.module.application.permission.Context
import org.solyton.solawi.bid.module.authentication.environment.JWT
import org.solyton.solawi.bid.module.authentication.environment.JwtEnv
import org.solyton.solawi.bid.module.permission.schema.ContextEntity
import org.solyton.solawi.bid.module.permission.schema.Contexts
import org.solyton.solawi.bid.module.permission.schema.RoleEntity
import org.solyton.solawi.bid.module.permission.schema.Roles
import org.solyton.solawi.bid.module.permission.schema.UserRoleContext
import org.solyton.solawi.bid.module.user.schema.UserEntity
import org.solyton.solawi.bid.module.user.schema.UserStatus
import org.solyton.solawi.bid.module.user.schema.Users
import java.sql.Connection
import org.jetbrains.exposed.sql.Database as SqlDatabase

fun Application.setupEnvironment(): Environment = with(environment.config){

    val environmentName: Environment.Name = property("ktor.environment").getString().let {
        when(it){
            "dev", "DEV","Dev", "D", "d" -> Environment.Name.DEV
            "test", "TEST", "Test", "t", "T" -> Environment.Name.TEST
            "int","Int", "INT", "Q", "q" -> Environment.Name.INT
            "prod", "PROD", "Prod", "P", "p" -> Environment.Name.PROD
            else -> throw IllegalArgumentException("Unknown environment name: $it")
        }
    }

    println("Running application on environment: $environmentName")

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
        environmentName,
        database,
        jwt,
        applicationOwner,
        mailService
    )
}

data class Environment(
    val name: Name = Name.PROD,
    val database: Database,
    override val jwt: JWT,
    val applicationOwner: User,
    val mailService: MailService,
    override val transformException: Result.Failure.Exception.() -> Pair<HttpStatusCode, Result.Failure.Message> = { transform() }
): KTorEnv, DbEnv, JwtEnv {
    lateinit var db: SqlDatabase

    enum class Name {
        DEV, TEST, INT, PROD
    }

    @OptIn(ExperimentalKeywordApi::class)
    override fun connectToDatabase(): SqlDatabase = when(::db.isInitialized){
        false-> with(SqlDatabase.connect(
            url = database.url,
            driver = database.driver,
            user = database.user,
            password = database.password,
            databaseConfig = DatabaseConfig {
                sqlLogger = when (name) {
                    Name.PROD -> null
                    else -> Slf4jSqlDebugLogger
                }
                useNestedTransactions = false
                defaultFetchSize = null // set 100, eg, only for  Resultsets/Streaming
                defaultIsolationLevel = Connection.TRANSACTION_READ_COMMITTED
                defaultRepetitionAttempts = if (name == Name.PROD) 3 else 1
                defaultMinRepetitionDelay = 100L
                defaultMaxRepetitionDelay = 1_000L
                defaultReadOnly = false
                warnLongQueriesDuration = when (name) {
                    Name.DEV, Name.TEST -> 300L
                    Name.INT -> 500L
                    Name.PROD -> 1_000L
                }
                maxEntitiesToStoreInCachePerEntity = 1_000
                keepLoadedReferencesOutOfTransaction = false
                explicitDialect = null
                defaultSchema = null
                logTooMuchResultSetsThreshold = 20
                preserveKeywordCasing = false
            }

        )) {
            db = this
            this
        }
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
                status = UserStatus.ACTIVE
                createdBy = UUID_ZERO
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
