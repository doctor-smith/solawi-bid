package org.solyton.solawi.bid.module.bid.application

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.respondText
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.solyton.solawi.bid.application.environment.setupEnvironment
import org.solyton.solawi.bid.application.pipeline.*
import org.solyton.solawi.bid.module.application.routing.application
import org.solyton.solawi.bid.module.authentication.migrations.authenticationMigrations
import org.solyton.solawi.bid.module.bid.routing.*
import org.solyton.solawi.bid.module.bid.routing.migrations.bidRoutingMigrations
import org.solyton.solawi.bid.module.testFramework.provideUserTokens
import org.solyton.solawi.bid.module.testFramework.testContexts
import org.solyton.solawi.bid.module.user.schema.OrganizationEntity
import org.solyton.solawi.bid.module.user.schema.OrganizationsTable
import org.solyton.solawi.bid.module.user.schema.UserEntity
import org.solyton.solawi.bid.module.user.schema.UserOrganization
import org.solyton.solawi.bid.module.user.schema.UsersTable


fun Application.bidTest() {
    val environment = setupEnvironment()
    installDatabase(environment, bidRoutingMigrations)
    installDatabase(environment, authenticationMigrations)
    installAuthentication(environment.jwt)
    installSerializers()
    installCors()
    installContentNegotiation()
    routing {
        val database = environment.connectToDatabase()
        provideUserTokens(environment.jwt, database)
        testContexts(database)
        get("setup/test-organization") {
            val organizationId = transaction(database) {
                SchemaUtils.create(UserOrganization)
                val organization = OrganizationEntity.find {
                    OrganizationsTable.name eq "TEST_ORGANIZATION"
                }.first()
                val owner = UserEntity.find { UsersTable.username eq "owner@solyton.org" }.first()
                UserOrganization.insert {
                    it[userId] = owner.id
                    it[this.organizationId] = organization.id
                }
                organization.id.value
            }
            call.respondText(organizationId.toString())
        }
        application(environment) {
            authenticate("auth-jwt"){ it() }
        }
        sendBid(environment)
        bid(environment){
            this.it()
        }
        auction(environment) {
            authenticate("auth-jwt"){ it() }
            // this.it()
        }
        round(environment){
            authenticate("auth-jwt"){ it() }
            // this.it()
        }
        bidders(environment){
            authenticate("auth-jwt"){ it() }
            // this.it()
        }
    }
}
