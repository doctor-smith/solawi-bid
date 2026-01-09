package org.evoleq.compose.guard.data

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.evoleq.compose.Markup
import org.evoleq.math.Get
import org.evoleq.math.Source
import org.evoleq.math.emit
import org.evoleq.math.on
import org.evoleq.math.write
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.transform.times
import org.solyton.solawi.bid.application.storage.middleware.ProcessAction
import org.solyton.solawi.bid.module.process.data.process.Process
import org.solyton.solawi.bid.module.process.data.process.ProcessState
import org.solyton.solawi.bid.module.process.data.processes.Processes
import org.solyton.solawi.bid.module.process.data.processes.Register
import org.solyton.solawi.bid.module.process.data.processes.UnRegister

import org.solyton.solawi.bid.module.process.data.processes.registry

@Composable fun <T> onMissing(
    source: Source<List<T>>,
    predicate: (T)-> Boolean,
    effect: @Composable ()->Unit
): Boolean {
    val missing = source.emit().none { predicate(it) }
    return if(missing){ effect(); true } else { false }
}

@Composable fun <T> onNullLaunch(
    source: Source<T?>,
    effect: @Composable ()->Unit
): Boolean {
    val isNull = source.emit() == null
    return if(isNull){ effect(); true } else { false }
}

@Composable fun <T> onNullTrigger(
    source: Source<T?>,
    effect: suspend ()->Unit
): Boolean {
    val isNull = source.emit() == null
    return if(isNull){
        rememberCoroutineScope().launch { effect() }
        true
    } else {
        false
    }
}

@Composable fun <T> onEmpty(
    source: Source<List<T>>,
    effect: @Composable ()->Unit
): Boolean {
    val empty = source.emit().isEmpty()
    return if(empty){ effect(); true } else { false }
}

@Composable fun <T> onEmpty(
    processId: String,
    processes: Storage<Processes>,
    source: Source<List<T>>,
    effect: @Composable ()->Unit
): Boolean {
    console.log("onEmpty called with processId: $processId")
    val processReader = processes * registry * Get(processId)
    val process = processReader.emit()
    if(process == null){
        console.log("process not found")
        val empty = source.emit().isEmpty()
        return if(empty){
            console.log("sources are empty ")
            console.log("launching register effect")
            CoroutineScope(Job()).launch {
                (processes * Register) write Process(processId) on Unit
            }
            console.log("launching effect")
            effect();
            true
        } else { false }
    }
    val state = process.state

    console.log("process found - state: $state")
    when(state){
        ProcessState.Finished -> CoroutineScope(Job()).launch {processes * UnRegister write processId on Unit}
        ProcessState.Active, ProcessState.Inactive -> Unit
    }
    return false
}

@Composable fun  onStringEmpty(
    source: Source<String>,
    effect: @Composable ()->Unit
): Boolean {
    val empty = source.emit().isEmpty()
    return if(empty){ effect(); true } else { false }
}
/*
@Composable fun loadOnce(effect: @Composable ()->Unit): Boolean {
    var isLoading by remember { mutableStateOf(false) }
    
    return if(!isLoading) {
        isLoading = true
        effect()
        true
    } else false
}

 */

@Composable fun isLoading(vararg booleans: Boolean): Boolean = booleans.any{ it }

@Markup
@Composable
fun withLoading(isLoading: Boolean, onLoading: @Composable () -> Unit, content: @Composable () -> Unit) =
    if(isLoading) onLoading() else content()
