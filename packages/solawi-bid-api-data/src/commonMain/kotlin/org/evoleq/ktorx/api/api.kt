package org.evoleq.ktorx.api

import org.evoleq.math.MathDsl
import org.evoleq.math.Reader
import kotlin.reflect.KClass

data class Api(
    val endPoints: HashMap<KClass<*>, EndPoint<*,*>> = hashMapOf()
): Map<KClass<*>, EndPoint<*,*>> by endPoints {

    fun < S, T> head(key: KClass<*>, url: String): Api = with(this) {
        endPoints[key] = EndPoint.Head<S,T>(url)
        this
    }
    fun < S, T> get(key: KClass<*>, url: String): Api = with(this) {
        endPoints[key] = EndPoint.Get<S,T>(url)
        this
    }
    fun < S, T> post(key: KClass<*>, url: String): Api = with(this) {
        endPoints[key] = EndPoint.Post<S,T>(url)
        this
    }
    fun < S, T> put(key: KClass<*>, url: String): Api = with(this) {
        endPoints[key] = EndPoint.Put<S,T>(url)
        this
    }
    fun < S, T> patch(key: KClass<*>, url: String): Api = with(this) {
        endPoints[key] = EndPoint.Patch<S,T>(url)
        this
    }
    fun < S, T> delete(key: KClass<*>, url: String): Api = with(this) {
        endPoints[key] = EndPoint.Delete<S,T>(url)
        this
    }


    inline fun <reified N: Any, S, T> get(url: String): Api = with(this) {
        endPoints[N::class] = EndPoint.Get<S,T>(url)
        this
    }
    inline fun <reified N: Any, S, T> post(url: String): Api = with(this) {
        endPoints[N::class] = EndPoint.Post<S, T>(url)
        this
    }
    inline fun <reified N: Any, S, T> put(url: String): Api = with(this) {
        endPoints[N::class] = EndPoint.Put<S,T>(url)
        this
    }
    inline fun <reified N: Any, S, T> patch(url: String): Api = with(this) {
        endPoints[N::class] = EndPoint.Patch<S,T>(url)
        this
    }
    inline fun <reified N: Any, S, T> delete(url: String): Api = with(this) {
        endPoints[N::class] = EndPoint.Delete<S,T>(url)
        this
    }
    inline fun <reified N: Any, S, T> head(url: String): Api = with(this) {
        endPoints[N::class] = EndPoint.Head<S,T>(url)
        this

    }

    operator fun invoke(configure: Api.()->Api): Api = Api().configure()

}

@MathDsl
@Suppress("UNCHECKED_CAST", "FunctionName")
fun <S, T> EndPoint(key: KClass<*>): Reader<Api, EndPoint<S, T>> = {api -> api[key]!! as EndPoint<S, T>}

sealed class EndPoint<in S, out T>(open val url: String) {
    data class Get<in S, out T>(override val url: String) : EndPoint<S, T>(url)
    data class Post<in S, out T>(override val url: String) : EndPoint<S, T>(url)
    data class Put<in S, out T>(override val url: String) : EndPoint<S, T>(url)
    data class Patch<in S, out T>(override val url: String) : EndPoint<S, T>(url)
    data class Delete<in S, out T>(override val url: String) : EndPoint<S, T>(url)
    data class Head<in S, out T>(override val url: String) : EndPoint<S, T>(url)
}

