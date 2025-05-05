package org.solyton.solawi.bid.application.ui.effect

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import org.evoleq.compose.Markup
import org.evoleq.language.LangComponent
import org.evoleq.math.Source
import org.evoleq.math.emit
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.storage.add
import org.evoleq.optics.transform.times
import org.solyton.solawi.bid.module.i18n.data.*
import org.solyton.solawi.bid.module.i18n.service.componentOnDemand

@Markup
@Suppress("FunctionName")
@Composable
fun LaunchComponentLookup(
    langComponent: LangComponent,
    environment: Source<Environment>,
    i18n: Storage<I18N>,
) {
    val loaded = (i18n * componentLoaded(langComponent)).emit()
    if(loaded) return

    LaunchedEffect(Unit) {
        val componentLookup = environment.emit()
            .componentOnDemand(
                langComponent,
                (i18n * language.get).emit(),
                (i18n * locale.get).emit()
            )

        (i18n * loadedComponents).add(langComponent)

        // replace component in storage
        if(componentLookup.mergeNeeded){
            (i18n * language).write(componentLookup.language)
        }
    }
}
