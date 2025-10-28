package org.solyton.solawi.bid.module.user.routing

// import org.solyton.solawi.bid.application.environment.Environment
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.util.*
import org.evoleq.exposedx.data.DbEnv
import org.evoleq.ktorx.Base
import org.evoleq.ktorx.Receive
import org.evoleq.ktorx.ReceiveContextual
import org.evoleq.ktorx.Respond
import org.evoleq.ktorx.data.KTorEnv
import org.evoleq.math.state.runOn
import org.evoleq.math.state.times
import org.solyton.solawi.bid.module.permission.action.db.IsGranted
import org.solyton.solawi.bid.module.user.action.ChangePassword
import org.solyton.solawi.bid.module.user.action.CreateNewUser
import org.solyton.solawi.bid.module.user.action.GetAllUsers
import org.solyton.solawi.bid.module.user.action.organization.CreateOrganization
import org.solyton.solawi.bid.module.user.action.organization.CreateChildOrganization
import org.solyton.solawi.bid.module.user.action.organization.ReadOrganizations
import org.solyton.solawi.bid.module.user.action.organization.UpdateOrganization
import org.solyton.solawi.bid.module.user.data.api.*
import org.solyton.solawi.bid.module.user.data.api.organization.CreateChildOrganization
import org.solyton.solawi.bid.module.user.data.api.organization.CreateOrganization
import org.solyton.solawi.bid.module.user.data.api.organization.ReadOrganizations
import org.solyton.solawi.bid.module.user.data.api.organization.UpdateOrganization

@KtorDsl
fun <UserEnv> Routing.user(
    environment: UserEnv,
    authenticate: Routing.(Route.() -> Route)-> Route
) where UserEnv : KTorEnv, UserEnv : DbEnv {
    val transform = environment.transformException
    authenticate {
        route("users") {
            get("all") {

                // val principal = call.authentication.principal<JWTPrincipal>()
                // val userId = principal?.payload?.subject ?: "Unknown"
                Receive(GetUsers) * GetAllUsers * Respond<Users>{ transform() } runOn Base(call, environment)
            }

            post("create") {
                ReceiveContextual<CreateUser>() * CreateNewUser * Respond<User>{ transform() } runOn Base(call, environment)
            }

            patch("change-password") {
                ReceiveContextual<ChangePassword>() * ChangePassword * Respond<User>{ transform() } runOn Base(call, environment)
            }
        }
    }
}


@KtorDsl
fun <OrganizationEnv> Routing.organization(
    environment: OrganizationEnv,
    authenticate: Routing.(Route.() -> Route)-> Route
) where OrganizationEnv : KTorEnv, OrganizationEnv : DbEnv {
    val transform = environment.transformException
    authenticate {
        route("organizations") {
            get("all") {
                ReceiveContextual(ReadOrganizations) *
                IsGranted("READ_ORGANIZATION") *
                ReadOrganizations() *
                Respond { transform() } runOn Base(call, environment)
            }

            post("create") {
                ReceiveContextual<CreateOrganization>() *
                IsGranted("CREATE_ORGANIZATION") *
                CreateOrganization() *
                Respond { transform() } runOn Base(call, environment)
            }
            post("create-child") {
                ReceiveContextual<CreateChildOrganization>() *
                IsGranted("CREATE_ORGANIZATION") *
                CreateChildOrganization() *
                Respond { transform() } runOn Base(call, environment)
            }
            patch("update") {
                ReceiveContextual<UpdateOrganization>() *
                IsGranted("UPDATE_ORGANIZATION") *
                UpdateOrganization() *
                Respond { transform() } runOn Base(call, environment)
            }
        }
    }
}
