package org.solyton.solawi.bid.application.storage.middleware.util

import org.evoleq.compose.modal.ModalData
import org.evoleq.compose.modal.ModalType
import org.evoleq.compose.style.data.device.DeviceType
import org.evoleq.device.data.mediaType
import org.evoleq.ktorx.result.Result
import org.evoleq.ktorx.result.Return
import org.evoleq.ktorx.result.apply
import org.evoleq.ktorx.result.on
import org.evoleq.math.*
import org.evoleq.math.state.KlState
import org.evoleq.math.state.State
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.storage.nextId
import org.evoleq.optics.storage.put
import org.evoleq.optics.transform.times
import org.solyton.solawi.bid.application.data.Application
import org.solyton.solawi.bid.application.data.deviceData
import org.solyton.solawi.bid.application.data.failure.Failure
import org.solyton.solawi.bid.application.data.failure.accept
import org.solyton.solawi.bid.application.data.modals
import org.solyton.solawi.bid.module.error.component.ErrorModal
import org.solyton.solawi.bid.module.error.lang.errorModalTexts


@MathDsl
@Suppress("FunctionName")
fun <T> Read(reader: Reader<Application, T>): State<Storage<Application>, T> = State {
    storage -> (storage * reader).emit() x storage
}

@MathDsl
@Suppress("FunctionName")
fun <T: Any> Dispatch(writer: Writer<Application, T>): KlState<Storage<Application>, Result<T>, Result<T>> = {
    result -> State { storage ->
        when(result) {
            is Result.Success -> Result.Return((storage * writer).dispatch()).apply() on result
            is Result.Failure -> Result.Return((storage.failureWriter()).dispatch()).apply() on result.accept()
        }
        result x storage
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
