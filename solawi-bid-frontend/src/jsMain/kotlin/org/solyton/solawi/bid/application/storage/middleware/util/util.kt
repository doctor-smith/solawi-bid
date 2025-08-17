package org.solyton.solawi.bid.application.storage.middleware.util

import org.evoleq.compose.modal.ModalData
import org.evoleq.compose.modal.ModalType
import org.evoleq.compose.style.data.device.DeviceType
import org.evoleq.device.data.mediaType
import org.evoleq.ktorx.result.Result
import org.evoleq.ktorx.result.Return
import org.evoleq.ktorx.result.apply
import org.evoleq.ktorx.result.map
import org.evoleq.ktorx.result.on
import org.evoleq.math.*
import org.evoleq.math.state.KlState
import org.evoleq.math.state.State
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.storage.nextId
import org.evoleq.optics.storage.put
import org.evoleq.optics.storage.write
import org.evoleq.optics.transform.times
import org.solyton.solawi.bid.application.data.Application
import org.solyton.solawi.bid.application.data.context
import org.solyton.solawi.bid.application.data.deviceData
import org.solyton.solawi.bid.application.data.failure.Failure
import org.solyton.solawi.bid.application.data.failure.accept
import org.solyton.solawi.bid.application.data.modals
import org.solyton.solawi.bid.application.service.setContext
import org.solyton.solawi.bid.module.context.data.Contextual
import org.solyton.solawi.bid.module.error.component.ErrorModal
import org.solyton.solawi.bid.module.error.lang.errorModalTexts


@MathDsl
@Suppress("FunctionName")
fun <T> Read(reader: Reader<Application, T>): State<Storage<Application>, T> = State {
    storage -> (storage * reader).emit() x storage
}

@MathDsl
@Suppress("FunctionName")
fun <T: Any> Dispatch(writer: Writer<Application, T>): KlState<Storage<Application>, Result<Contextual<T>>, Result<T>> = {
    result -> State { storage ->
        // todo:dev do it more functional - maybe using apply
        val newResult = result map { contextual ->
            storage.setContext(contextual.context)
            contextual.data
        }
        when(newResult) {
            is Result.Success -> Result.Return((storage * writer).dispatch()).apply() on newResult
            is Result.Failure -> Result.Return((storage.failureWriter()).dispatch()).apply() on newResult.accept()
        }
        newResult x storage
    }
}

@Suppress("UNCHECKED_CAST")
fun Storage<Application>.failureWriter(): Writer<Unit, Failure> = Writer{
    failure: Failure -> {
        val modals = this * modals
        val nextId = modals.nextId()
        modals.put(
            item = nextId to ModalData(
                ModalType.Error,
                ErrorModal(
                    id = nextId,
                    texts = errorModalTexts(failure.message),
                    modals = modals,

                    device = (this@failureWriter * deviceData * mediaType.get) as Source<DeviceType>,
                )
            )
        )
    }
}
