package org.evoleq.ktorx.api

import org.evoleq.math.MathDsl
import org.evoleq.math.Reader
import kotlin.collections.set
import kotlin.reflect.KClass

@Suppress("TooManyFunctions")
data class Api(
    val endPoints: HashMap<KClass<*>, EndPoint<*,*>> = hashMapOf()
): Map<KClass<*>, EndPoint<*,*>> by endPoints {

    inline fun < reified S, reified T> head(key: KClass<*>, url: String): Api = with(this) {
        endPoints[key] = EndPoint.Head<S,T>(url, S::class, T::class)
        this
    }
    inline fun < reified S, reified T> get(key: KClass<*>, url: String): Api = with(this) {
        endPoints[key] = EndPoint.Get<S,T>(url, S::class, T::class)
        this
    }
    inline fun < reified S, reified T> post(key: KClass<*>, url: String): Api = with(this) {
        endPoints[key] = EndPoint.Post<S,T>(url, S::class, T::class)
        this
    }
    inline fun < reified S, reified T> put(key: KClass<*>, url: String): Api = with(this) {
        endPoints[key] = EndPoint.Put<S,T>(url, S::class, T::class)
        this
    }
    inline fun < reified S, reified T> patch(key: KClass<*>, url: String): Api = with(this) {
        endPoints[key] = EndPoint.Patch<S,T>(url, S::class, T::class)
        this
    }
    inline fun < reified S, reified T> delete(key: KClass<*>, url: String): Api = with(this) {
        endPoints[key] = EndPoint.Delete<S,T>(url, S::class, T::class)
        this
    }

    inline fun <reified N: Any, reified S, reified T> get(url: String): Api = with(this) {
        endPoints[N::class] = EndPoint.Get<S,T>(url, S::class, T::class)
        this
    }
    inline fun <reified N: Any, reified S, reified T> post(url: String): Api = with(this) {
        endPoints[N::class] = EndPoint.Post<S, T>(url, S::class, T::class)
        this
    }
    inline fun <reified N: Any, reified S, reified T> put(url: String): Api = with(this) {
        endPoints[N::class] = EndPoint.Put<S,T>(url, S::class, T::class)
        this
    }
    inline fun <reified N: Any,  reified S, reified T> patch(url: String): Api = with(this) {
        endPoints[N::class] = EndPoint.Patch<S,T>(url, S::class, T::class)
        this
    }
    inline fun <reified N: Any,  reified S, reified T> delete(url: String): Api = with(this) {
        endPoints[N::class] = EndPoint.Delete<S,T>(url, S::class, T::class)
        this
    }
    inline fun <reified N: Any,  reified S, reified T> head(url: String): Api = with(this) {
        endPoints[N::class] = EndPoint.Head<S,T>(url, S::class, T::class)
        this

    }

    // Helper to copy endpoints with new url
    private fun <S, T> EndPoint<S, T>.withUrl(newUrl: String): EndPoint<S, T> = when(this) {
        is EndPoint.Get -> copy(url = newUrl)
        is EndPoint.Post -> copy(url = newUrl)
        is EndPoint.Put -> copy(url = newUrl)
        is EndPoint.Patch -> copy(url = newUrl)
        is EndPoint.Delete -> copy(url = newUrl)
        is EndPoint.Head -> copy(url = newUrl)
    }

    // Helper to copy endpoints with new group
    private fun <S, T> EndPoint<S, T>.withGroup(newGroup: String): EndPoint<S, T> = when(this) {
        is EndPoint.Get -> copy(group = newGroup)
        is EndPoint.Post -> copy(group = newGroup)
        is EndPoint.Put -> copy(group = newGroup)
        is EndPoint.Patch -> copy(group = newGroup)
        is EndPoint.Delete -> copy(group = newGroup)
        is EndPoint.Head -> copy(group = newGroup)
    }


    @Suppress("UNUSED_PARAMETER")
    fun group(name: String, configure: Api.() -> Api): Api = with(this) {
        val subApi = Api().configure()
        subApi.endPoints.forEach { (key, value) ->
            endPoints[key] = value.withGroup(name).withUrl(value.url)
        }
        this
    }

    fun module(path: String, module: Api): Api = with(this) {
        module.endPoints.forEach { (key, value) ->
            endPoints[key] = value.withGroup(path).withUrl("$path/${value.url}")
        }
        this
    }
    fun module(path: String, configure: Api.() -> Api): Api = with(this) {
        val subApi = Api().configure()
        subApi.endPoints.forEach { (key, value) ->
            endPoints[key] = value.withGroup(path).withUrl("$path/${value.url}")
         }
        this
    }

    companion object {
        operator fun invoke(configure: Api.() -> Api): Api = Api().configure()
    }
}

@MathDsl
@Suppress("UNCHECKED_CAST", "FunctionName")
fun <S, T> EndPoint(key: KClass<*>): Reader<Api, EndPoint<S, T>> = {api -> api[key]!! as EndPoint<S, T>}

sealed class EndPoint<in S, out T>(
    open val url: String,
    open val requestType: KClass<*>,
    open val responseType: KClass<*>,
    open val group: String? = null
) {
    data class Get<in S, out T>(
        override val url: String,
        override val requestType: KClass<*>,
        override val responseType: KClass<*>,
        override val group: String? = null
    ) : EndPoint<S, T>(
        url,
        requestType,
        responseType,
        group
    )
    data class Post<in S, out T>(
        override val url: String,
        override val requestType: KClass<*>,
        override val responseType: KClass<*>,
        override val group: String? = null
    ) : EndPoint<S, T>(
        url,
        requestType,
        responseType,
        group
    )
    data class Put<in S, out T>(
        override val url: String,
        override val requestType: KClass<*>,
        override val responseType: KClass<*>,
        override val group: String? = null
    ) : EndPoint<S, T>(
        url,
        requestType,
        responseType,
        group
    )
    data class Patch<in S, out T>(
        override val url: String,
        override val requestType: KClass<*>,
        override val responseType: KClass<*>,
        override val group: String? = null
    ): EndPoint<S, T>(
        url,
        requestType,
        responseType,
        group
    )
    data class Delete<in S, out T>(
        override val url: String,
        override val requestType: KClass<*>,
        override val responseType: KClass<*>,
        override val group: String? = null
    ) : EndPoint<S, T>(
        url,
        requestType,
        responseType,
        group
    )
    data class Head<in S, out T>(
        override val url: String,
        override val requestType: KClass<*>,
        override val responseType: KClass<*>,
        override val group: String? = null
    ) : EndPoint<S, T>(
        url,
        requestType,
        responseType,
        group
    )
}
