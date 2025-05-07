package org.evoleq.ktorx.client

import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.http.*
import kotlinx.serialization.json.Json
import org.evoleq.ktorx.api.Api
import org.evoleq.ktorx.result.Result
import org.evoleq.ktorx.result.ResultSerializer
import org.evoleq.ktorx.result.Serializer
import org.evoleq.ktorx.result.serializers
import org.jetbrains.compose.web.testutils.ComposeWebExperimentalTestsApi
import org.jetbrains.compose.web.testutils.runTest
import org.solyton.solawi.bid.application.api.solawiApi
import org.solyton.solawi.bid.application.api.post
import org.solyton.solawi.bid.application.serialization.installSerializers
import org.solyton.solawi.bid.module.authentication.data.api.LoggedIn
import org.solyton.solawi.bid.module.authentication.data.api.Login
import org.solyton.solawi.bid.module.authentication.data.api.Logout
import org.solyton.solawi.bid.module.authentication.data.api.RefreshToken
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertTrue

class HttpRequestTests {



    @OptIn(ComposeWebExperimentalTestsApi::class)
    @Test
    fun check() = runTest{
        installSerializers()
        val mockEngine = solawiApi.toMockEngine()
        assertTrue { serializers[Login::class] != null }
        assertTrue { serializers[LoggedIn::class] != null }

        val login = Login("username", "password") as Any
        assertTrue { serializers[login::class] != null }
        assertTrue { Serializer(login) == Login.serializer() }

        val client = HttpClient(mockEngine)

        val result = client.post<Login, LoggedIn>("login", 9876, Serializer(), ResultSerializer()) (login as Login)
        assertIs<Result.Success<LoggedIn>>(result)
        console.log(result)
    }

}
@OptIn(ComposeWebExperimentalTestsApi::class)

// @Test
fun checkOnDispatch() = runTest{
    /*
    installSerializers()
    var application by remember { mutableStateOf<Application>(Application(
        environment = Environment("DEV")
    )) }
    val storage = Storage<Application>(
        read = { application },
        write = {
                newApplication -> application = newApplication
        }
    ).onDispatch {
        (this@onDispatch * actions).read().collect { action ->

            CallApi(action as Action<Application, Any, Any>
            ) runOn this
        }
    }

    (storage * actions).read().emit(Action<Application,Login, LoggedIn>(
        "login",
        reader = {app:Application -> Login(app.userData.username, app.userData.password)},
        endPoint = Login::class,
        writer = {loggedIn: LoggedIn ->{app: Application -> app.copy(
            userData = app.userData.copy(
                accessToken = loggedIn.accessToken,
                refreshToken = loggedIn.refreshToken
            )
        )} }
    ))
    */
}


val sampleResponses = mapOf(
    Login::class to LoggedIn("session", "access-token", "refresh-token"),
    RefreshToken::class to LoggedIn("session", "access-token", "refresh-token"),
    Logout::class to Unit
)

fun Api.toMockEngine(): MockEngine = MockEngine{
    request -> try{with(this@toMockEngine.entries.find { "/${it.value.url}" == request.url.fullPath }!!.key) {
        respond(
            content = Json.encodeToString(ResultSerializer, Result.Success(sampleResponses[this]!!))
        )} }
        catch(exception: Exception) {
            respond(
                content = Json.encodeToString(ResultSerializer, Result.Failure.Exception(exception))
            )
        }
}