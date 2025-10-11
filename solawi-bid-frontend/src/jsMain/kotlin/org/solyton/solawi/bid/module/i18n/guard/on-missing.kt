package org.solyton.solawi.bid.module.i18n.guard

import androidx.compose.runtime.Composable
import org.evoleq.language.LangComponent
import org.evoleq.math.Source
import org.evoleq.math.emit
import org.evoleq.math.times
import org.solyton.solawi.bid.module.i18n.data.I18N
import org.solyton.solawi.bid.module.i18n.data.componentLoaded

@Composable fun onMissing(
    component: LangComponent,
    storage: Source<I18N>,
    effect: @Composable ()->Unit
): Boolean {
    val missing = !(storage * componentLoaded(component)).emit()
    return if(missing){ effect(); true } else { false }
}
