package org.solyton.solawi.bid.application.plugin


/*
class AuthenticationHolder private constructor(config: Configuration) {

    class Configuration

    companion object : Plugin<Application, Configuration, AuthenticationHolder> {
        override val key = AttributeKey<AuthenticationHolder>("AuthenticationHolder")

        override fun install(pipeline: Application, configure: Configuration.() -> Unit): AuthenticationHolder {
            // pipeline.pluginOrNull(Authentication) ?: throw MissingApplicationPluginException("Authentication")

            val configuration = Configuration().apply(configure)
            return AuthenticationHolder(configuration)
        }

        // Utility function to retrieve the JWT principal
        fun ApplicationCall.jwtPrincipal(): JWTPrincipal? {
            return authentication.principal<JWTPrincipal>()
        }
    }
}
*/