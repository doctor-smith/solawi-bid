package org.evoleq.optics.storage

import kotlinx.coroutines.flow.MutableSharedFlow
import org.evoleq.optics.lens.Lens

data class ActionEnvelope<Base : Any>(
    val action: Action<Base, *, *>,
    val meta: Map<String, Any?> = emptyMap()
)

interface ActionDispatcher<Base : Any> {
    suspend infix fun <I : Any, O : Any> dispatch(action: Action<Base, I, O>)
    suspend infix fun dispatchEnvelope(envelope: ActionEnvelope<Base>)
}

data class MutableSharedFlowActionDispatcher<Base : Any>(
    val flow: MutableSharedFlow<ActionEnvelope<Base>>
) : ActionDispatcher<Base> {

    override suspend fun <I : Any, O : Any> dispatch(action: Action<Base, I, O>) {
        flow.emit(ActionEnvelope(action))
    }

    override suspend fun dispatchEnvelope(envelope: ActionEnvelope<Base>) {
        flow.emit(envelope)
    }

    suspend fun <I : Any, O : Any> emit(action: Action<Base, I, O>) = dispatch(action)
}

operator fun <Whole : Any, Part : Any> ActionDispatcher<Whole>.times(
    lens: Lens<Whole, Part>
): ActionDispatcher<Part> = object : ActionDispatcher<Part> {

    override suspend fun <I : Any, O : Any> dispatch(action: Action<Part, I, O>) {
        this@times.dispatch(lens * action)
    }

    override suspend fun dispatchEnvelope(envelope: ActionEnvelope<Part>) {
        this@times.dispatchEnvelope(
            ActionEnvelope(
                action = lens * envelope.action,
                meta = envelope.meta
            )
        )
    }
}

operator fun <Whole : Any, Part : Any> MutableSharedFlow<ActionEnvelope<Whole>>.times(
    lens: Lens<Whole, Part>
): ActionDispatcher<Part> = object : ActionDispatcher<Part> {

    override suspend infix fun <I : Any, O : Any> dispatch(action: Action<Part, I, O>) {
        emit(ActionEnvelope(lens * action))
    }

    override suspend infix fun dispatchEnvelope(envelope: ActionEnvelope<Part>) {
        emit(ActionEnvelope(action = lens * envelope.action, meta = envelope.meta))
    }
}

fun <Base : Any> ActionDispatcher(
    dispatch: suspend (ActionEnvelope<Base>) -> Unit
): ActionDispatcher<Base> = object : ActionDispatcher<Base> {

    override suspend infix fun <I : Any, O : Any> dispatch(action: Action<Base, I, O>) {
        dispatch(ActionEnvelope(action))
    }

    override suspend infix fun dispatchEnvelope(envelope: ActionEnvelope<Base>) {
        dispatch(envelope)
    }
}

suspend infix fun <Base : Any> Storage<ActionDispatcher<Base>>.dispatch(
    envelope: ActionEnvelope<Base>
) {
    read().dispatchEnvelope(envelope)
}


suspend infix fun <Base : Any> Storage<ActionDispatcher<Base>>.dispatch(
    action: Action<Base, *,  *>
) {
    read().dispatch(action)
}
