package org.evoleq.optics.storage

import kotlinx.coroutines.flow.MutableSharedFlow
import org.evoleq.language.BasePath
import org.evoleq.optics.lens.Lens


interface ActionDispatcher<Base: Any> {
    suspend fun <I: Any, O:Any> dispatch(action: Action<Base,I,O> ){}
}

data class MutableSharedFlowActionDispatcher<Base : Any>(val flow: MutableSharedFlow<Action<Base,*,*>>) : ActionDispatcher<Base> {
    override suspend fun <I: Any, O:Any> dispatch(action: Action<Base,I,O>) = flow.emit(action)

    suspend fun <I: Any, O:Any> emit(action: Action<Base,I,O>) = dispatch(action)
}

operator fun <Whole: Any, Part: Any> ActionDispatcher<Whole>.times(lens: Lens<Whole, Part>): ActionDispatcher<Part> = object : ActionDispatcher<Part> {

    override suspend fun <I : Any, O : Any> dispatch(action: Action<Part, I, O>) = dispatch(lens * action)
}

operator fun <Whole: Any, Part: Any> MutableSharedFlow<Action<Whole, *, *>>.times(lens: Lens<Whole, Part>): ActionDispatcher<Part> = object : ActionDispatcher<Part> {
    override suspend fun <I : Any, O : Any> dispatch(action: Action<Part, I, O>) = emit(lens * action)
}

fun <Base:Any> ActionDispatcher(dispatch: suspend (Action<Base, *,*>)->Unit): ActionDispatcher<Base> = object : ActionDispatcher<Base> {
    override suspend fun <I : Any, O : Any> dispatch(action: Action<Base, I, O>) = dispatch(action)
}
