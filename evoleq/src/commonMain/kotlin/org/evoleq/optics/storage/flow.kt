package org.evoleq.optics.storage

import kotlinx.coroutines.flow.MutableSharedFlow
import org.evoleq.optics.lens.Lens

data class ActionEnvelope<Base : Any, out I : Any, O : Any>(
    val action: Action<Base, I, O>,
    val id: String,
    val parentId: String? = null,
    val next: List<ActionEnvelope<Base, *, *>> = emptyList(),
    val meta: Map<String, Any?> = emptyMap(),
    val run: Boolean = true
)

interface ActionDispatcher<Base : Any> {
    suspend infix fun <I : Any, O : Any> dispatch(action: Action<Base, I, O>)
    suspend infix fun <I : Any, O : Any> dispatchEnvelope(envelope: ActionEnvelope<Base, I, O>)
}

data class MutableSharedFlowActionDispatcher<Base : Any>(
    val flow: MutableSharedFlow<ActionEnvelope<Base, *, *>>
) : ActionDispatcher<Base> {

    override suspend fun <I : Any, O : Any> dispatch(action: Action<Base, I, O>) {
        flow.emit(ActionEnvelope(action, id = action.name))
    }

    override suspend fun <I : Any, O : Any> dispatchEnvelope(envelope: ActionEnvelope<Base, I, O>) {
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

    override suspend fun <I : Any, O : Any> dispatchEnvelope(envelope: ActionEnvelope<Part, I, O>) {
        this@times.dispatchEnvelope(
            ActionEnvelope(
                action = lens * envelope.action,
                meta = envelope.meta,
                id = envelope.id,
                parentId = envelope.parentId,
            )
        )
    }
}

operator fun <Whole : Any, Part : Any> MutableSharedFlow<ActionEnvelope<Whole, * ,*>>.times(
    lens: Lens<Whole, Part>
): ActionDispatcher<Part> = object : ActionDispatcher<Part> {

    override suspend infix fun <I : Any, O : Any> dispatch(action: Action<Part, I, O>) {
        emit(ActionEnvelope(lens * action, action.name))
    }

    override suspend infix fun <I : Any, O : Any> dispatchEnvelope(envelope: ActionEnvelope<Part, I, O>) {
        emit(ActionEnvelope(
            action = lens * envelope.action,
            id = envelope.id,
            parentId = envelope.parentId,
            meta = envelope.meta
        ))
    }
}

fun <Base : Any> ActionDispatcher(
    dispatch: suspend (ActionEnvelope<Base, *, *>) -> Unit
): ActionDispatcher<Base> = object : ActionDispatcher<Base> {

    override suspend infix fun <I : Any, O : Any> dispatch(action: Action<Base, I, O>) {
        dispatch(ActionEnvelope(
            action, action.name, null,
        ))
    }

    override suspend infix fun <I : Any, O : Any> dispatchEnvelope(envelope: ActionEnvelope<Base, I, O>) {
        dispatch(envelope)
    }
}

suspend infix fun <Base : Any> Storage<ActionDispatcher<Base>>.dispatch(
    envelope: ActionEnvelope<Base, *, *>
) {
    read().dispatchEnvelope(envelope)
}


suspend infix fun <Base : Any> Storage<ActionDispatcher<Base>>.dispatch(
    action: Action<Base, *,  *>
) {
    read().dispatch(action)
}
